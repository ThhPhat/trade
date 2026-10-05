package vn.bookstore.the4bookstore.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;

    public CustomOAuth2UserService(TaiKhoanRepository taiKhoanRepository, KhachHangRepository khachHangRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"),
                    "Không thể lấy được địa chỉ Email từ tài khoản Google."
            );
        }

        String sub = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        if (name == null || name.isBlank()) {
            Object given = oAuth2User.getAttribute("given_name");
            Object family = oAuth2User.getAttribute("family_name");
            if (given != null || family != null) {
                name = ((family != null ? family.toString() + " " : "") + (given != null ? given.toString() : "")).trim();
            }
        }
        String picture = oAuth2User.getAttribute("picture");
        if (picture == null || picture.isBlank()) {
            Object pic = oAuth2User.getAttribute("avatar_url");
            if (pic != null) {
                picture = pic.toString();
            }
        }

        try {
            TaiKhoan taiKhoan = processOAuthUser(email, sub, name, picture, registrationId);

            String nameAttributeKey = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
            if (nameAttributeKey == null || !oAuth2User.getAttributes().containsKey(nameAttributeKey)) {
                nameAttributeKey = "sub";
            }

            return new CustomOAuth2User(taiKhoan, oAuth2User.getAttributes(), nameAttributeKey);
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error processing OAuth user: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth_processing_failed", "Lỗi xử lý tài khoản Google: " + e.getMessage(), null), e);
        }
    }

    public TaiKhoan processOAuthUser(String email, String sub, String name, String picture, String registrationId) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        String provider = (registrationId != null && !registrationId.isBlank()) ? registrationId.toUpperCase() : "GOOGLE";
        String safeSub = (sub != null && sub.length() > 100) ? sub.substring(0, 100) : sub;

        // Chuẩn hóa tên hiển thị thật từ Google
        String displayName = (name != null && !name.isBlank() && !name.matches("\\d+"))
                ? name.trim()
                : (normalizedEmail.contains("@") ? normalizedEmail.split("@")[0] : "Khách Hàng");
        if (displayName.length() > 100) {
            displayName = displayName.substring(0, 100);
        }

        String safePicture = picture;
        if (safePicture != null && safePicture.length() > 200) {
            safePicture = safePicture.substring(0, 200);
        }

        // Tìm tài khoản theo email hoặc theo providerId
        Optional<TaiKhoan> optionalTaiKhoan = taiKhoanRepository.findByEmail(normalizedEmail);
        if (optionalTaiKhoan.isEmpty() && safeSub != null && !safeSub.isBlank()) {
            optionalTaiKhoan = taiKhoanRepository.findByProviderId(safeSub);
        }

        TaiKhoan tk;

        if (optionalTaiKhoan.isPresent()) {
            tk = optionalTaiKhoan.get();
            boolean needUpdateTk = false;

            if (tk.getProviderId() == null && safeSub != null) {
                tk.setProviderId(safeSub);
                needUpdateTk = true;
            }
            if (tk.getAuthProvider() == null || "LOCAL".equalsIgnoreCase(tk.getAuthProvider())) {
                tk.setAuthProvider(provider);
                needUpdateTk = true;
            }
            // Nếu tên đăng nhập cũ là chuỗi số hoặc rỗng, cập nhật thành tên đăng nhập chuẩn hóa
            if (tk.getTenDangNhap() == null || tk.getTenDangNhap().isBlank() || tk.getTenDangNhap().matches("\\d+")) {
                String candidate = generateSafeUsername(normalizedEmail, displayName);
                tk.setTenDangNhap(candidate);
                needUpdateTk = true;
            }
            java.util.List<String> adminEmails = java.util.List.of(
                    "caotuong14@gmail.com", "caotuong06@gmail.com", "24110375@student.hcmute.edu.vn"
            );
            boolean isAdmin = adminEmails.contains(normalizedEmail) || normalizedEmail.contains("admin");

            if (isAdmin && !"ADMIN".equalsIgnoreCase(tk.getVaiTro())) {
                tk.setVaiTro("ADMIN");
                needUpdateTk = true;
            }

            if (needUpdateTk) {
                try {
                    tk = taiKhoanRepository.save(tk);
                } catch (Exception e) {
                    System.err.println("Notice: Could not update TaiKhoan during OAuth: " + e.getMessage());
                }
            }

            // Đồng bộ trực tiếp Họ tên và Ảnh đại diện thực từ tài khoản Google vào KhachHang
            try {
                KhachHang kh = khachHangRepository.findByTaiKhoan(tk).orElse(null);
                if (kh == null) {
                    kh = khachHangRepository.findByEmail(normalizedEmail).orElse(null);
                }
                if (kh == null) {
                    kh = new KhachHang();
                    kh.setTaiKhoan(tk);
                }

                if (kh.getHoTen() == null || kh.getHoTen().isBlank() || kh.getHoTen().matches("\\d+") || kh.getHoTen().contains("@")) {
                    kh.setHoTen(displayName);
                }
                kh.setEmail(normalizedEmail);
                if (safePicture != null && !safePicture.isBlank()) {
                    kh.setAnhDaiDien(safePicture);
                }
                if (kh.getNgayDangKy() == null) {
                    kh.setNgayDangKy(LocalDateTime.now());
                }
                khachHangRepository.save(kh);
            } catch (Exception e) {
                System.err.println("Notice: Could not sync KhachHang during OAuth: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // TỰ ĐỘNG TẠO TÀI KHOẢN MỚI CHO NGƯỜI DÙNG ĐĂNG NHẬP GOOGLE CHƯA ĐĂNG KÝ
            java.util.List<String> adminEmails = java.util.List.of(
                    "caotuong14@gmail.com", "caotuong06@gmail.com", "24110375@student.hcmute.edu.vn"
            );
            boolean isAdmin = adminEmails.contains(normalizedEmail) || normalizedEmail.contains("admin");

            tk = new TaiKhoan();
            String candidate = generateSafeUsername(normalizedEmail, displayName);
            tk.setTenDangNhap(candidate);
            tk.setEmail(normalizedEmail);
            tk.setMatKhauHash("OAUTH2_" + UUID.randomUUID());
            tk.setVaiTro(isAdmin ? "ADMIN" : "KHACHHANG");
            tk.setTrangThai("HoatDong");
            tk.setAuthProvider(provider);
            tk.setProviderId(safeSub);
            tk.setNgayTao(LocalDateTime.now());
            tk = taiKhoanRepository.save(tk);

            // Tự động khởi tạo hồ sơ Khách hàng đồng bộ với tài khoản Google
            try {
                KhachHang newKh = khachHangRepository.findByEmail(normalizedEmail).orElse(null);
                if (newKh == null) {
                    newKh = new KhachHang();
                }
                newKh.setTaiKhoan(tk);
                newKh.setHoTen(displayName);
                newKh.setEmail(normalizedEmail);
                if (safePicture != null && !safePicture.isBlank()) {
                    newKh.setAnhDaiDien(safePicture);
                }
                if (newKh.getNgayDangKy() == null) {
                    newKh.setNgayDangKy(LocalDateTime.now());
                }
                khachHangRepository.save(newKh);
            } catch (Exception e) {
                System.err.println("Notice: Could not create KhachHang during OAuth: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return tk;
    }

    private String generateSafeUsername(String email, String displayName) {
        String base = "";
        // Ưu tiên tạo username chuẩn không dấu từ Họ Tên Google (nếu họ tên không phải là chuỗi số)
        if (displayName != null && !displayName.isBlank() && !displayName.matches("\\d+")) {
            String normalized = java.text.Normalizer.normalize(displayName, java.text.Normalizer.Form.NFD);
            String ascii = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "_")
                    .replaceAll("_+", "_")
                    .replaceAll("^_|_$", "");
            if (!ascii.isBlank()) {
                base = ascii;
            }
        }
        // Fallback sang tiền tố email nếu cần
        if (base.isBlank() && email != null && email.contains("@")) {
            base = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
        }
        if (base.isBlank()) {
            base = "user";
        }
        if (base.length() > 30) {
            base = base.substring(0, 30);
        }

        String candidate = base;
        while (taiKhoanRepository.findByTenDangNhap(candidate).isPresent()) {
            candidate = base + "_" + UUID.randomUUID().toString().substring(0, 4);
            if (candidate.length() > 45) {
                candidate = candidate.substring(0, 45);
            }
        }
        return candidate;
    }
}
