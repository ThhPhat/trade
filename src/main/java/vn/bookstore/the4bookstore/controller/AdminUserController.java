package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.NhanVien;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.NhanVienRepository;
import vn.bookstore.the4bookstore.repository.ShopRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomUserDetails;

import org.springframework.security.access.prepost.PreAuthorize;
import vn.bookstore.the4bookstore.service.SecurityAuditService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'QUANLY')")
public class AdminUserController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.security.core.session.SessionRegistry sessionRegistry;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SecurityAuditService securityAuditService;

    private boolean isSuperAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    private void invalidateUserSessions(TaiKhoan tk) {
        if (sessionRegistry == null || tk == null) return;
        try {
            for (Object principal : sessionRegistry.getAllPrincipals()) {
                boolean match = false;
                if (principal instanceof CustomUserDetails ud && ud.getTaiKhoan() != null) {
                    if (ud.getTaiKhoan().getMaTaiKhoan().equals(tk.getMaTaiKhoan())) match = true;
                } else if (principal instanceof CustomOAuth2User o && o.getTaiKhoan() != null) {
                    if (o.getTaiKhoan().getMaTaiKhoan().equals(tk.getMaTaiKhoan())) match = true;
                } else if (principal instanceof UserDetails ud) {
                    if (ud.getUsername().equalsIgnoreCase(tk.getTenDangNhap()) || ud.getUsername().equalsIgnoreCase(tk.getEmail())) {
                        match = true;
                    }
                }
                if (match) {
                    for (SessionInformation sessionInfo : sessionRegistry.getAllSessions(principal, false)) {
                        sessionInfo.expireNow();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 15);
        Page<TaiKhoan> userPage = taiKhoanRepository.searchAccounts(keyword, role, pageRequest);

        // Nạp thông tin hồ sơ liên kết (Khách hàng, Nhân viên, Shop)
        Map<Integer, KhachHang> customerProfiles = new HashMap<>();
        Map<Integer, NhanVien> staffProfiles = new HashMap<>();
        Map<Integer, Shop> shopProfiles = new HashMap<>();

        for (TaiKhoan tk : userPage.getContent()) {
            khachHangRepository.findByTaiKhoan(tk).ifPresent(kh -> customerProfiles.put(tk.getMaTaiKhoan(), kh));
            nhanVienRepository.findByTaiKhoan(tk).ifPresent(nv -> staffProfiles.put(tk.getMaTaiKhoan(), nv));
            shopRepository.findByTaiKhoan(tk).ifPresent(sp -> shopProfiles.put(tk.getMaTaiKhoan(), sp));
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("page", userPage);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedRole", role != null ? role : "");
        model.addAttribute("customerProfiles", customerProfiles);
        model.addAttribute("staffProfiles", staffProfiles);
        model.addAttribute("shopProfiles", shopProfiles);

        // Số liệu thống kê
        model.addAttribute("totalAccounts", taiKhoanRepository.count());
        model.addAttribute("totalUsers", taiKhoanRepository.countByVaiTro("USER"));
        model.addAttribute("totalVendors", taiKhoanRepository.countByVaiTro("VENDOR"));
        model.addAttribute("totalStaff", taiKhoanRepository.countByVaiTro("MANAGER") + taiKhoanRepository.countByVaiTro("ADMIN") + taiKhoanRepository.countByVaiTro("NHANVIENKHO"));
        model.addAttribute("totalLocked", taiKhoanRepository.countByTrangThai("BiKhoa"));

        return "admin/users";
    }

    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(
            @PathVariable Integer id,
            Authentication auth,
            RedirectAttributes ra) {
        Optional<TaiKhoan> opt = taiKhoanRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tài khoản không tồn tại!");
            return "redirect:/admin/users";
        }

        TaiKhoan tk = opt.get();

        // Không cho phép tự khóa tài khoản của chính mình
        if (auth != null && (auth.getName().equalsIgnoreCase(tk.getEmail()) || auth.getName().equalsIgnoreCase(tk.getTenDangNhap()))) {
            ra.addFlashAttribute("errorMessage", "Bạn không thể tự khóa tài khoản đang đăng nhập của chính mình!");
            return "redirect:/admin/users";
        }

        // Quyền Manager thấp hơn Admin: không cho phép Manager can thiệp/khóa tài khoản Admin
        if (!isSuperAdmin(auth) && "ADMIN".equalsIgnoreCase(tk.getVaiTro())) {
            ra.addFlashAttribute("errorMessage", "Quản lý (Manager) không có quyền khóa hoặc can thiệp tài khoản Quản trị viên cấp cao (Admin)!");
            return "redirect:/admin/users";
        }

        boolean willLock = !"BiKhoa".equalsIgnoreCase(tk.getTrangThai());
        tk.setTrangThai(willLock ? "BiKhoa" : "HoatDong");
        // Tăng tokenVersion để vô hiệu hóa tất cả JWT đang lưu hành của tài khoản này
        tk.setTokenVersion(tk.getTokenVersion() == null ? 1 : tk.getTokenVersion() + 1);
        taiKhoanRepository.save(tk);

        if (willLock) {
            invalidateUserSessions(tk);
            if (securityAuditService != null) {
                securityAuditService.logAccountLocked(tk.getTenDangNhap(), auth != null ? auth.getName() : "SYSTEM");
            }
        } else {
            if (securityAuditService != null) {
                securityAuditService.logAccountUnlocked(tk.getTenDangNhap(), auth != null ? auth.getName() : "SYSTEM");
            }
        }

        String msg = willLock ? "Đã KHÓA tài khoản @" + tk.getTenDangNhap() + " thành công (đã hủy phiên đăng nhập nếu có)!" : "Đã MỞ KHÓA tài khoản @" + tk.getTenDangNhap() + " thành công!";
        ra.addFlashAttribute("successMessage", msg);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/change-role")
    public String changeRole(
            @PathVariable Integer id,
            @RequestParam String newRole,
            Authentication auth,
            RedirectAttributes ra) {
        Optional<TaiKhoan> opt = taiKhoanRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tài khoản không tồn tại!");
            return "redirect:/admin/users";
        }

        TaiKhoan tk = opt.get();

        // Không cho phép tự hạ quyền của chính mình nếu đang là ADMIN
        if (auth != null && (auth.getName().equalsIgnoreCase(tk.getEmail()) || auth.getName().equalsIgnoreCase(tk.getTenDangNhap()))) {
            if ("ADMIN".equalsIgnoreCase(tk.getVaiTro()) && !"ADMIN".equalsIgnoreCase(newRole)) {
                ra.addFlashAttribute("errorMessage", "Bạn không thể tự hạ quyền ADMIN của chính mình!");
                return "redirect:/admin/users";
            }
        }

        // Quyền Manager thấp hơn Admin: không cho phép Manager sửa vai trò Admin hoặc tự nâng quyền lên Admin
        if (!isSuperAdmin(auth) && ("ADMIN".equalsIgnoreCase(tk.getVaiTro()) || "ADMIN".equalsIgnoreCase(newRole))) {
            ra.addFlashAttribute("errorMessage", "Quản lý (Manager) không có quyền thay đổi vai trò của Admin hoặc nâng cấp tài khoản lên Admin!");
            return "redirect:/admin/users";
        }

        String upperRole = newRole.trim().toUpperCase();
        String oldRole = tk.getVaiTro();
        tk.setVaiTro(upperRole);
        // Tăng tokenVersion để buộc người dùng đăng nhập lại với quyền mới
        tk.setTokenVersion(tk.getTokenVersion() == null ? 1 : tk.getTokenVersion() + 1);
        taiKhoanRepository.save(tk);
        if (securityAuditService != null) {
            securityAuditService.logRoleChange(tk.getTenDangNhap(), oldRole, upperRole, auth != null ? auth.getName() : "SYSTEM");
        }

        // Tự động đồng bộ / tạo hồ sơ Nhân viên (NhanVien) chỉ khi là vai trò nhân viên kho
        if ("NHANVIENKHO".equals(upperRole)) {
            Optional<NhanVien> optNv = nhanVienRepository.findByTaiKhoan(tk);
            NhanVien nv;
            if (optNv.isPresent()) {
                nv = optNv.get();
                nv.setTrangThai("DangLam");
                nv.setChucVu("Nhân viên kho");
            } else {
                nv = new NhanVien();
                nv.setTaiKhoan(tk);
                nv.setNgayVaoLam(java.time.LocalDate.now());
                nv.setTrangThai("DangLam");
                nv.setChucVu("Nhân viên kho");
                nv.setEmail(tk.getEmail());

                Optional<KhachHang> optKh = khachHangRepository.findByTaiKhoan(tk);
                String hoTen = optKh.map(KhachHang::getHoTen).filter(s -> s != null && !s.isBlank()).orElse(tk.getTenDangNhap());
                String sdt = optKh.map(KhachHang::getSoDienThoai).filter(s -> s != null && !s.isBlank()).orElse("09" + String.format("%08d", tk.getMaTaiKhoan()));
                String diaChi = optKh.map(KhachHang::getDiaChi).orElse(null);

                nv.setHoTen(hoTen);
                nv.setSoDienThoai(sdt);
                nv.setDiaChi(diaChi);
            }
            nhanVienRepository.save(nv);
        } else {
            // Không phải vai trò nhân viên kho (ADMIN, MANAGER, USER, VENDOR) -> đánh dấu Đã Nghỉ
            nhanVienRepository.findByTaiKhoan(tk).ifPresent(nv -> {
                nv.setTrangThai("DaNghi");
                nhanVienRepository.save(nv);
            });
        }

        // Hủy phiên đăng nhập cũ để người dùng nhận vai trò mới khi truy cập lại
        invalidateUserSessions(tk);

        ra.addFlashAttribute("successMessage", "Đã cập nhật vai trò tài khoản @" + tk.getTenDangNhap() + " thành [" + newRole + "] thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Integer id,
            @RequestParam String newPassword,
            Authentication auth,
            RedirectAttributes ra) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            ra.addFlashAttribute("errorMessage", "Mật khẩu mới phải có tối thiểu 6 ký tự!");
            return "redirect:/admin/users";
        }

        Optional<TaiKhoan> opt = taiKhoanRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tài khoản không tồn tại!");
            return "redirect:/admin/users";
        }

        TaiKhoan tk = opt.get();

        // Quyền Manager thấp hơn Admin: không cho phép đặt lại mật khẩu cho Admin
        if (!isSuperAdmin(auth) && "ADMIN".equalsIgnoreCase(tk.getVaiTro())) {
            ra.addFlashAttribute("errorMessage", "Quản lý (Manager) không có quyền đặt lại mật khẩu cho tài khoản Quản trị viên cấp cao (Admin)!");
            return "redirect:/admin/users";
        }

        tk.setMatKhauHash(passwordEncoder.encode(newPassword.trim()));
        taiKhoanRepository.save(tk);

        // Hủy phiên đăng nhập cũ để buộc đăng nhập lại bằng mật khẩu mới
        invalidateUserSessions(tk);

        ra.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu mới cho tài khoản @" + tk.getTenDangNhap() + " thành công!");
        return "redirect:/admin/users";
    }
}
