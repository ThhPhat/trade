package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.DiaChiGiaoHang;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.service.DiaChiService;

import java.util.List;

@Controller
@RequestMapping("/profile/addresses")
@RequiredArgsConstructor
public class DiaChiController {

    private final DiaChiService diaChiService;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    private KhachHang getCurrentKhachHang(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        TaiKhoan tk = taiKhoanRepository.findByEmail(auth.getName())
                .or(() -> taiKhoanRepository.findByTenDangNhap(auth.getName()))
                .orElse(null);
        if (tk == null) return null;
        return khachHangRepository.findByTaiKhoan(tk).orElse(null);
    }

    @GetMapping
    public String addressList(Authentication auth, Model model) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        List<DiaChiGiaoHang> addresses = diaChiService.getAddresses(kh);
        model.addAttribute("khachHang", kh);
        model.addAttribute("addresses", addresses);
        model.addAttribute("newAddress", new DiaChiGiaoHang());
        return "profile/addresses";
    }

    @PostMapping("/add")
    public String addAddress(Authentication auth,
                             @ModelAttribute DiaChiGiaoHang diaChi,
                             RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        try {
            diaChiService.addAddress(kh, diaChi);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ nhận hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/set-default/{id}")
    public String setDefault(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        try {
            diaChiService.setDefaultAddress(kh, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt làm địa chỉ mặc định!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        try {
            diaChiService.deleteAddress(kh, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa chỉ thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }
}
