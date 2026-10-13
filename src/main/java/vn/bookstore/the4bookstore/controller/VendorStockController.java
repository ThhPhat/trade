package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import vn.bookstore.the4bookstore.service.CapHangService;
import vn.bookstore.the4bookstore.service.KhoService;
import vn.bookstore.the4bookstore.service.ShopService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@Controller
@RequestMapping("/vendor/stock-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'MANAGER')")
public class VendorStockController {

    private final CapHangService capHangService;
    private final ShopService shopService;
    private final KhoService khoService;
    private final SanPhamRepository sanPhamRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    private TaiKhoan getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        TaiKhoan tk = null;
        Object p = auth.getPrincipal();
        if (p instanceof CustomUserDetails ud && ud.getTaiKhoan() != null && ud.getTaiKhoan().getMaTaiKhoan() != null) {
            tk = taiKhoanRepository.findById(ud.getTaiKhoan().getMaTaiKhoan()).orElse(ud.getTaiKhoan());
        } else if (p instanceof CustomOAuth2User o && o.getTaiKhoan() != null && o.getTaiKhoan().getMaTaiKhoan() != null) {
            tk = taiKhoanRepository.findById(o.getTaiKhoan().getMaTaiKhoan()).orElse(o.getTaiKhoan());
        } else if (p instanceof CustomOidcUser o && o.getTaiKhoan() != null && o.getTaiKhoan().getMaTaiKhoan() != null) {
            tk = taiKhoanRepository.findById(o.getTaiKhoan().getMaTaiKhoan()).orElse(o.getTaiKhoan());
        } else if (p instanceof CustomOAuth2User o && o.getEmail() != null) {
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        }
        return tk;
    }

    @GetMapping
    public String listRequests(@RequestParam(defaultValue = "0") int page,
                               Authentication auth,
                               Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        Page<YeuCauCapHang> requests = capHangService.getRequestsByShop(shop, PageRequest.of(page, 10));
        model.addAttribute("shop", shop);
        model.addAttribute("requests", requests);
        model.addAttribute("currentPage", page);
        return "vendor/stock_requests";
    }

    @GetMapping("/create")
    public String showCreateForm(Authentication auth, Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        List<Kho> khos = khoService.getAllKho();
        List<SanPham> products = sanPhamRepository.findByShop_MaShop(shop.getMaShop());

        model.addAttribute("shop", shop);
        model.addAttribute("khos", khos);
        model.addAttribute("products", products);
        return "vendor/stock_request_form";
    }

    @PostMapping("/create")
    public String submitRequest(@RequestParam Integer maKho,
                                @RequestParam(required = false) String ghiChu,
                                @RequestParam("productIds") List<Integer> productIds,
                                @RequestParam("quantities") List<Integer> quantities,
                                Authentication auth,
                                RedirectAttributes ra) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        try {
            Map<Integer, Integer> items = new HashMap<>();
            for (int i = 0; i < productIds.size(); i++) {
                Integer pid = productIds.get(i);
                Integer qty = (quantities != null && i < quantities.size()) ? quantities.get(i) : 0;
                if (pid != null && qty != null && qty > 0) {
                    items.put(pid, qty);
                }
            }

            if (items.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 sản phẩm và nhập số lượng yêu cầu hợp lệ!");
                return "redirect:/vendor/stock-requests/create";
            }

            capHangService.createRequest(shop, maKho, ghiChu, items);
            ra.addFlashAttribute("successMessage", "Yêu cầu cấp hàng đã được gửi thành công lên Tổng bộ kho hàng!");
            return "redirect:/vendor/stock-requests";
        } catch (Exception e) {
            log.error("Loi tao yeu cau cap hang: ", e);
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/vendor/stock-requests/create";
        }
    }
}
