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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;

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

        boolean willLock = !"BiKhoa".equalsIgnoreCase(tk.getTrangThai());
        tk.setTrangThai(willLock ? "BiKhoa" : "HoatDong");
        taiKhoanRepository.save(tk);

        String msg = willLock ? "Đã KHÓA tài khoản @" + tk.getTenDangNhap() + " thành công!" : "Đã MỞ KHÓA tài khoản @" + tk.getTenDangNhap() + " thành công!";
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

        tk.setVaiTro(newRole.trim().toUpperCase());
        taiKhoanRepository.save(tk);

        ra.addFlashAttribute("successMessage", "Đã cập nhật vai trò tài khoản @" + tk.getTenDangNhap() + " thành [" + newRole + "] thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Integer id,
            @RequestParam String newPassword,
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
        tk.setMatKhauHash(passwordEncoder.encode(newPassword.trim()));
        taiKhoanRepository.save(tk);

        ra.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu mới cho tài khoản @" + tk.getTenDangNhap() + " thành công!");
        return "redirect:/admin/users";
    }
}
