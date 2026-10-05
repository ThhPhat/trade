package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.service.UserInteractionService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserInteractionController {

    private final UserInteractionService interactionService;
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

    // --- DANH SÁCH YÊU THÍCH (WISHLIST) ---
    @GetMapping("/profile/favorites")
    public String wishlistPage(Authentication auth, Model model) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        List<SanPham> favorites = interactionService.getFavorites(kh);
        model.addAttribute("khachHang", kh);
        model.addAttribute("favorites", favorites);
        return "profile/favorites";
    }

    @PostMapping("/api/favorites/toggle/{maSP}")
    @ResponseBody
    public ResponseEntity<?> toggleFavorite(Authentication auth, @PathVariable Integer maSP) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để yêu thích sản phẩm"));
        }
        boolean isFav = interactionService.toggleFavorite(kh, maSP);
        return ResponseEntity.ok(Map.of("success", true, "isFavorite", isFav));
    }

    // --- SẢN PHẨM ĐÃ XEM ---
    @GetMapping("/profile/recently-viewed")
    public String recentlyViewedPage(Authentication auth, Model model) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        List<SanPham> recentlyViewed = interactionService.getRecentlyViewed(kh);
        model.addAttribute("khachHang", kh);
        model.addAttribute("recentlyViewed", recentlyViewed);
        return "profile/recently_viewed";
    }

    // --- ĐÁNH GIÁ SẢN PHẨM ---
    @PostMapping("/api/danh-gia/submit")
    public String submitReview(Authentication auth,
                               @RequestParam Integer maSP,
                               @RequestParam Integer maDH,
                               @RequestParam int soSao,
                               @RequestParam String noiDung,
                               @RequestParam(required = false) String mediaUrl,
                               RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(auth);
        if (kh == null) return "redirect:/login";

        try {
            List<String> mediaUrls = (mediaUrl != null && !mediaUrl.isBlank()) ? Arrays.asList(mediaUrl.split(",")) : List.of();
            interactionService.submitReview(kh, maSP, maDH, soSao, noiDung, mediaUrls);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá chi tiết cho sản phẩm!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/san-pham/" + maSP;
    }
}
