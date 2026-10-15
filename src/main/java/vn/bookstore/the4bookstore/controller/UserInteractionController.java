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
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserInteractionController {

    private final UserInteractionService interactionService;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    private TaiKhoan getCurrentTaiKhoan(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof vn.bookstore.the4bookstore.security.CustomUserDetails userDetails) {
            if (userDetails.getTaiKhoan() != null && userDetails.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(userDetails.getTaiKhoan().getMaTaiKhoan())
                        .orElse(userDetails.getTaiKhoan());
            }
        }

        if (principal instanceof vn.bookstore.the4bookstore.security.CustomOAuth2User oAuth2User) {
            if (oAuth2User.getTaiKhoan() != null && oAuth2User.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(oAuth2User.getTaiKhoan().getMaTaiKhoan())
                        .orElse(oAuth2User.getTaiKhoan());
            }
            if (oAuth2User.getEmail() != null && !oAuth2User.getEmail().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(oAuth2User.getEmail());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        if (principal instanceof vn.bookstore.the4bookstore.security.CustomOidcUser oidcUser) {
            if (oidcUser.getTaiKhoan() != null && oidcUser.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(oidcUser.getTaiKhoan().getMaTaiKhoan())
                        .orElse(oidcUser.getTaiKhoan());
            }
            if (oidcUser.getEmail() != null && !oidcUser.getEmail().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(oidcUser.getEmail());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(email);
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            Object emailObj = oauth2User.getAttribute("email");
            if (emailObj != null && !emailObj.toString().isBlank()) {
                Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByEmail(emailObj.toString());
                if (tkOpt.isPresent()) return tkOpt.get();
            }
        }

        String authName = authentication.getName();
        if (authName != null && !authName.isBlank()) {
            return taiKhoanRepository.findByEmail(authName)
                    .or(() -> taiKhoanRepository.findByTenDangNhap(authName))
                    .orElse(null);
        }

        return null;
    }

    private KhachHang getCurrentKhachHang(Authentication auth) {
        TaiKhoan tk = getCurrentTaiKhoan(auth);
        if (tk == null) return null;

        KhachHang kh = khachHangRepository.findByTaiKhoan(tk).orElse(null);
        if (kh == null && tk.getEmail() != null && !tk.getEmail().isBlank()) {
            kh = khachHangRepository.findByEmail(tk.getEmail()).orElse(null);
        }
        if (kh == null) {
            kh = new KhachHang();
            kh.setHoTen(tk.getTenDangNhap() != null ? tk.getTenDangNhap() : "Khách Hàng");
            kh.setEmail(tk.getEmail());
            kh.setTaiKhoan(tk);
            kh.setNgayDangKy(java.time.LocalDateTime.now());
            try {
                kh = khachHangRepository.save(kh);
            } catch (Exception ignored) {
            }
        }
        return kh;
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
