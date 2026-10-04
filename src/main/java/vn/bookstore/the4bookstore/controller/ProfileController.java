package vn.bookstore.the4bookstore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.DonHang;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.entity.VoucherDaLuu;
import vn.bookstore.the4bookstore.repository.DonHangRepository;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.KhuyenMaiRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.repository.VoucherDaLuuRepository;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@org.springframework.web.bind.annotation.RequestMapping("/profile")
public class ProfileController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final DonHangRepository donHangRepository;
        private final VoucherDaLuuRepository voucherDaLuuRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(TaiKhoanRepository taiKhoanRepository,
                             KhachHangRepository khachHangRepository,
                             DonHangRepository donHangRepository,
                                                          VoucherDaLuuRepository voucherDaLuuRepository,
                             PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.donHangRepository = donHangRepository;
                this.voucherDaLuuRepository = voucherDaLuuRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private TaiKhoan getCurrentTaiKhoan(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // 1. Form Login (CustomUserDetails)
        if (principal instanceof CustomUserDetails userDetails) {
            if (userDetails.getTaiKhoan() != null && userDetails.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(userDetails.getTaiKhoan().getMaTaiKhoan())
                        .orElse(userDetails.getTaiKhoan());
            }
        }

        // 2. CustomOAuth2User
        if (principal instanceof CustomOAuth2User oAuth2User) {
            if (oAuth2User.getTaiKhoan() != null && oAuth2User.getTaiKhoan().getMaTaiKhoan() != null) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findById(oAuth2User.getTaiKhoan().getMaTaiKhoan());
                if (tkOpt.isPresent()) {
                    return tkOpt.get();
                }
            }
            if (oAuth2User.getEmail() != null && !oAuth2User.getEmail().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(oAuth2User.getEmail());
                if (tkOpt.isPresent()) {
                    return tkOpt.get();
                }
            }
        }

        // 3. CustomOidcUser
        if (principal instanceof CustomOidcUser oidcUser) {
            if (oidcUser.getTaiKhoan() != null && oidcUser.getTaiKhoan().getMaTaiKhoan() != null) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findById(oidcUser.getTaiKhoan().getMaTaiKhoan());
                if (tkOpt.isPresent()) {
                    return tkOpt.get();
                }
            }
            if (oidcUser.getEmail() != null && !oidcUser.getEmail().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(oidcUser.getEmail());
                if (tkOpt.isPresent()) {
                    return tkOpt.get();
                }
            }
        }

        // 4. Standard OidcUser
        if (principal instanceof OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(email);
                if (tkOpt.isPresent()) {
                    return tkOpt.get();
                }
            }
        }

        // 5. Standard OAuth2User
        if (principal instanceof OAuth2User oauth2User) {
            Object emailObj = oauth2User.getAttribute("email");
            if (emailObj != null && !emailObj.toString().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(emailObj.toString());
                if (tkOpt.isPresent()) {
                    return tkOpt.get();
                }
            }
        }

        // 6. Username / Email fallback
        String authName = authentication.getName();
        if (authName != null && !authName.isBlank()) {
            return taiKhoanRepository.findByEmail(authName)
                    .or(() -> taiKhoanRepository.findByTenDangNhap(authName))
                    .orElse(null);
        }

        return null;
    }

    @GetMapping
    public String profilePage(Authentication authentication, Model model) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) {
            return "redirect:/login";
        }

        // Lấy hoặc khởi tạo đối tượng Khách Hàng an toàn
        KhachHang kh = khachHangRepository.findByTaiKhoan(tk).orElse(null);
        if (kh == null) {
            kh = new KhachHang();
            kh.setHoTen(tk.getTenDangNhap());
            kh.setEmail(tk.getEmail());
            kh.setTaiKhoan(tk);
            kh.setNgayDangKy(LocalDateTime.now());
            try {
                kh = khachHangRepository.save(kh);
            } catch (Exception ignored) {
                // Giữ lại instance kh trong bộ nhớ cho view hiển thị nếu gặp ràng buộc DB
            }
        }

        // Tự động kiểm tra và đồng bộ dữ liệu tên thật và avatar từ tài khoản Google
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String gName = (String) oauth2User.getAttributes().get("name");
            if (gName == null || gName.isBlank()) {
                Object given = oauth2User.getAttribute("given_name");
                Object family = oauth2User.getAttribute("family_name");
                if (given != null || family != null) {
                    gName = ((family != null ? family.toString() + " " : "") + (given != null ? given.toString() : "")).trim();
                }
            }
            String gPic = (String) oauth2User.getAttributes().get("picture");
            if (gPic == null || gPic.isBlank()) {
                Object picObj = oauth2User.getAttribute("avatar_url");
                if (picObj != null) gPic = picObj.toString();
            }

            boolean needSaveKh = false;
            boolean needSaveTk = false;

            if (gName != null && !gName.isBlank() && !gName.matches("\\d+")) {
                if (kh != null && (kh.getHoTen() == null || kh.getHoTen().isBlank() || kh.getHoTen().matches("\\d+") || kh.getHoTen().contains("@"))) {
                    kh.setHoTen(gName);
                    needSaveKh = true;
                }
                if (tk.getTenDangNhap() == null || tk.getTenDangNhap().matches("\\d+")) {
                    tk.setTenDangNhap(gName);
                    needSaveTk = true;
                }
            }
            if (gPic != null && !gPic.isBlank()) {
                if (kh != null && (kh.getAnhDaiDien() == null || kh.getAnhDaiDien().isBlank())) {
                    kh.setAnhDaiDien(gPic);
                    needSaveKh = true;
                }
            }

            if (needSaveTk) {
                try {
                    tk = taiKhoanRepository.save(tk);
                } catch (Exception ignored) {}
            }
            if (needSaveKh && kh != null) {
                try {
                    kh = khachHangRepository.save(kh);
                } catch (Exception ignored) {}
            }
        }

        // Lấy danh sách đơn hàng an toàn khi kh đã có maKH trong DB
        List<DonHang> donHangs = Collections.emptyList();
        Long totalOrders = 0L;
        Long completedOrders = 0L;

        if (kh != null && kh.getMaKH() != null) {
            try {
                List<DonHang> list = donHangRepository.findByKhachHangOrderByNgayDatDesc(kh);
                if (list != null) {
                    donHangs = list;
                }
                Long cnt = donHangRepository.countByKhachHang(kh);
                if (cnt != null) {
                    totalOrders = cnt;
                }
                Long cmp = donHangRepository.countByKhachHangAndTrangThai(kh, "DaGiao");
                if (cmp != null) {
                    completedOrders = cmp;
                }
            } catch (Exception ignored) {
            }
        }

        // Lấy voucher đã thu thập vào Ví cá nhân (chỉ những mã user đã bấm "Lưu mã")
        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> availableVouchers = Collections.emptyList();
        long expiringSoonCount = 0;
        try {
            if (kh != null && kh.getMaKH() != null) {
                List<VoucherDaLuu> savedVouchers = voucherDaLuuRepository
                        .findByKhachHangAndTrangThaiOrderByNgayLuuDesc(kh, "ChuaDung");
                availableVouchers = savedVouchers.stream()
                        .map(VoucherDaLuu::getKhuyenMai)
                        .filter(km -> km.isDangDienRa())
                        .toList();
                expiringSoonCount = availableVouchers.stream()
                        .filter(km -> km.getNgayKetThuc() != null && km.getNgayKetThuc().isBefore(now.plusDays(3)))
                        .count();
            }
        } catch (Exception ignored) {
        }

        model.addAttribute("taiKhoan", tk);
        model.addAttribute("khachHang", kh);
        model.addAttribute("donHangs", donHangs);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("availableVouchers", availableVouchers);
        model.addAttribute("totalVouchers", availableVouchers.size());
        model.addAttribute("expiringSoonCount", expiringSoonCount);
        model.addAttribute("activePage", "profile");

        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(Authentication authentication,
                                @RequestParam("hoTen") String hoTen,
                                @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
                                @RequestParam(value = "diaChi", required = false) String diaChi,
                                RedirectAttributes redirectAttributes) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) {
            return "redirect:/login";
        }

        KhachHang kh = khachHangRepository.findByTaiKhoan(tk).orElseGet(() -> {
            KhachHang newKh = new KhachHang();
            newKh.setTaiKhoan(tk);
            newKh.setEmail(tk.getEmail());
            newKh.setNgayDangKy(LocalDateTime.now());
            return newKh;
        });

        kh.setHoTen(hoTen != null ? hoTen.trim() : tk.getTenDangNhap());
        kh.setSoDienThoai(soDienThoai != null && !soDienThoai.isBlank() ? soDienThoai.trim() : null);
        kh.setDiaChi(diaChi != null && !diaChi.isBlank() ? diaChi.trim() : null);
        khachHangRepository.save(kh);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
        return "redirect:/profile?updated=true";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam("matKhauCu") String matKhauCu,
                                 @RequestParam("matKhauMoi") String matKhauMoi,
                                 @RequestParam("xacNhanMatKhau") String xacNhanMatKhau,
                                 RedirectAttributes redirectAttributes) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) {
            return "redirect:/login";
        }

        if (tk.getAuthProvider() != null && "GOOGLE".equalsIgnoreCase(tk.getAuthProvider())) {
            redirectAttributes.addFlashAttribute("pwdError", "Tài khoản của bạn được liên kết qua Google. Mật khẩu được bảo mật và quản lý trực tiếp bởi Google.");
            return "redirect:/profile?pwdError=google";
        }

        if (tk.getMatKhauHash() != null && !passwordEncoder.matches(matKhauCu, tk.getMatKhauHash())) {
            redirectAttributes.addFlashAttribute("pwdError", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/profile?pwdError=incorrect";
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            redirectAttributes.addFlashAttribute("pwdError", "Xác nhận mật khẩu mới không trùng khớp!");
            return "redirect:/profile?pwdError=mismatch";
        }

        if (matKhauMoi.length() < 6) {
            redirectAttributes.addFlashAttribute("pwdError", "Mật khẩu mới phải có tối thiểu 6 ký tự!");
            return "redirect:/profile?pwdError=short";
        }

        tk.setMatKhauHash(passwordEncoder.encode(matKhauMoi));
        taiKhoanRepository.save(tk);

        redirectAttributes.addFlashAttribute("pwdSuccess", "Đổi mật khẩu thành công!");
        return "redirect:/profile?pwdSuccess=true";
    }

    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(Authentication authentication,
                               @RequestParam("avatarFile") MultipartFile avatarFile,
                               RedirectAttributes redirectAttributes) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) {
            return "redirect:/login";
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("avatarError", "Vui lòng chọn tệp ảnh hợp lệ!");
            return "redirect:/profile";
        }

        if (avatarFile.getSize() > 5 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("avatarError", "Dung lượng ảnh không được vượt quá 5MB!");
            return "redirect:/profile";
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
            redirectAttributes.addFlashAttribute("avatarError", "Định dạng tệp không được hỗ trợ! Vui lòng chọn ảnh JPG, PNG hoặc WebP.");
            return "redirect:/profile";
        }

        String originalFilename = avatarFile.getOriginalFilename();
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!extension.matches("\\.(png|jpg|jpeg|webp|gif)")) {
                extension = ".png";
            }
        }

        try {
            Path uploadDir = Paths.get("uploads", "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = "avatar_" + tk.getMaTaiKhoan() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path filePath = uploadDir.resolve(filename);

            try (InputStream inputStream = avatarFile.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            KhachHang kh = khachHangRepository.findByTaiKhoan(tk).orElseGet(() -> {
                KhachHang newKh = new KhachHang();
                newKh.setTaiKhoan(tk);
                newKh.setEmail(tk.getEmail());
                newKh.setHoTen(tk.getTenDangNhap());
                newKh.setNgayDangKy(LocalDateTime.now());
                return newKh;
            });

            kh.setAnhDaiDien("/uploads/avatars/" + filename);
            khachHangRepository.save(kh);

            redirectAttributes.addFlashAttribute("avatarSuccess", "Tải lên ảnh đại diện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("avatarError", "Đã xảy ra lỗi khi lưu ảnh: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/remove-avatar")
    public String removeAvatar(Authentication authentication, RedirectAttributes redirectAttributes) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) {
            return "redirect:/login";
        }

        Optional<KhachHang> khOpt = khachHangRepository.findByTaiKhoan(tk);
        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            kh.setAnhDaiDien(null);
            khachHangRepository.save(kh);
            redirectAttributes.addFlashAttribute("avatarSuccess", "Đã gỡ ảnh đại diện, chuyển về sử dụng avatar chữ cái đầu!");
        }

        return "redirect:/profile";
    }
}
