package vn.bookstore.the4bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentUserService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;

    public TaiKhoan getCurrentTaiKhoan(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // 1. CustomUserDetails
        if (principal instanceof CustomUserDetails userDetails && userDetails.getTaiKhoan() != null) {
            if (userDetails.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(userDetails.getTaiKhoan().getMaTaiKhoan())
                        .orElse(userDetails.getTaiKhoan());
            }
            return userDetails.getTaiKhoan();
        }

        // 2. CustomOAuth2User
        if (principal instanceof CustomOAuth2User oAuth2User) {
            if (oAuth2User.getTaiKhoan() != null && oAuth2User.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(oAuth2User.getTaiKhoan().getMaTaiKhoan())
                        .orElse(oAuth2User.getTaiKhoan());
            }
            if (oAuth2User.getEmail() != null && !oAuth2User.getEmail().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(oAuth2User.getEmail().trim().toLowerCase());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        // 3. CustomOidcUser
        if (principal instanceof CustomOidcUser oidcUser) {
            if (oidcUser.getTaiKhoan() != null && oidcUser.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(oidcUser.getTaiKhoan().getMaTaiKhoan())
                        .orElse(oidcUser.getTaiKhoan());
            }
            if (oidcUser.getEmail() != null && !oidcUser.getEmail().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(oidcUser.getEmail().trim().toLowerCase());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        // 4. Standard OidcUser
        if (principal instanceof OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(email.trim().toLowerCase());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        // 5. Standard OAuth2User
        if (principal instanceof OAuth2User oauth2User) {
            Object emailObj = oauth2User.getAttribute("email");
            if (emailObj != null && !emailObj.toString().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(emailObj.toString().trim().toLowerCase());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        // 6. Name / Email / Username fallback
        String authName = authentication.getName();
        if (authName != null && !authName.isBlank()) {
            return taiKhoanRepository.findByEmail(authName.trim().toLowerCase())
                    .or(() -> taiKhoanRepository.findByTenDangNhap(authName.trim()))
                    .orElse(null);
        }

        return null;
    }

    @Transactional
    public KhachHang getCurrentKhachHang(Authentication authentication) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) return null;
        return getOrCreateKhachHang(tk);
    }

    @Transactional
    public KhachHang getOrCreateKhachHang(TaiKhoan tk) {
        if (tk == null) return null;

        // 1. Tìm theo TaiKhoan
        Optional<KhachHang> khOpt = khachHangRepository.findByTaiKhoan(tk);
        if (khOpt.isPresent()) {
            return khOpt.get();
        }

        // 2. Tìm theo Email nếu TaiKhoan có email
        if (tk.getEmail() != null && !tk.getEmail().isBlank()) {
            Optional<KhachHang> byEmail = khachHangRepository.findByEmail(tk.getEmail().trim().toLowerCase());
            if (byEmail.isPresent()) {
                KhachHang kh = byEmail.get();
                if (kh.getTaiKhoan() == null) {
                    kh.setTaiKhoan(tk);
                    return khachHangRepository.save(kh);
                }
                return kh;
            }
        }

        // 3. Tạo mới nếu chưa có
        KhachHang newKh = new KhachHang();
        newKh.setTaiKhoan(tk);
        String hoTen = tk.getTenDangNhap();
        if (hoTen == null || hoTen.isBlank() || hoTen.matches("\\d+")) {
            if (tk.getEmail() != null && tk.getEmail().contains("@")) {
                hoTen = tk.getEmail().split("@")[0];
            } else {
                hoTen = "Khách Hàng";
            }
        }
        newKh.setHoTen(hoTen);
        newKh.setEmail(tk.getEmail());
        newKh.setNgayDangKy(LocalDateTime.now());

        try {
            return khachHangRepository.save(newKh);
        } catch (Exception e) {
            log.warn("Could not save new KhachHang for TaiKhoan {}: {}", tk.getMaTaiKhoan(), e.getMessage());
            return khachHangRepository.findByTaiKhoan(tk)
                    .or(() -> (tk.getEmail() != null && !tk.getEmail().isBlank())
                            ? khachHangRepository.findByEmail(tk.getEmail())
                            : Optional.empty())
                    .orElse(newKh);
        }
    }
}
