package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.DonHang;
import vn.bookstore.the4bookstore.entity.NhaVanChuyen;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.service.ManagerDisputeService;
import vn.bookstore.the4bookstore.service.ShopService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ManagerDisputeController {

    private final ShopService shopService;
    private final ManagerDisputeService disputeService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final SanPhamRepository sanPhamRepository;

    private TaiKhoan getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return taiKhoanRepository.findByEmail(auth.getName())
                .or(() -> taiKhoanRepository.findByTenDangNhap(auth.getName()))
                .orElse(null);
    }

    // --- QUẢN LÝ CỬA HÀNG (SHOPS MANAGEMENT) ---
    @GetMapping("/shops")
    public String shopsList(@RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Page<Shop> shopPage = (keyword != null && !keyword.isBlank())
                ? shopService.searchShops(keyword, PageRequest.of(page, 10))
                : shopService.getAllShops(PageRequest.of(page, 10));

        Map<Integer, Long> productCounts = new HashMap<>();
        for (Shop s : shopPage.getContent()) {
            productCounts.put(s.getMaShop(), sanPhamRepository.countByShop_MaShop(s.getMaShop()));
        }

        model.addAttribute("shops", shopPage.getContent());
        model.addAttribute("productCounts", productCounts);
        model.addAttribute("page", shopPage);
        model.addAttribute("keyword", keyword);
        return "admin/shops";
    }

    @PostMapping("/shops/{id}/approve")
    public String approveShop(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            shopService.duyetShop(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt và kích hoạt gian hàng #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/shops";
    }

    @PostMapping("/shops/{id}/lock")
    public String lockShop(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            shopService.khoaShop(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa gian hàng #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/shops";
    }

    @PostMapping("/shops/{id}/unlock")
    public String unlockShop(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            shopService.moKhoaShop(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa gian hàng #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/shops";
    }

    @PostMapping("/shops/{id}/delete")
    public String deleteShop(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            shopService.xoaShop(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa gian hàng #" + id + " và khóa toàn bộ sản phẩm của gian hàng!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/shops";
    }

    @PostMapping("/shops/{id}/commission")
    public String updateShopCommission(@PathVariable Integer id,
                                       @RequestParam BigDecimal commissionRate,
                                       RedirectAttributes redirectAttributes) {
        try {
            shopService.updateChietKhau(id, commissionRate);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tỷ lệ chiết khấu cho shop #" + id + " thành " + commissionRate + "%");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/shops";
    }

    // --- XEM SẢN PHẨM CỦA GIAN HÀNG (CHỈ XEM THÔNG TIN / KHÓA VI PHẠM) ---
    @GetMapping("/shops/{id}/products")
    public String shopProducts(@PathVariable Integer id,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        Shop shop = shopService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gian hàng: " + id));

        Page<SanPham> productPage;
        if (keyword != null && !keyword.isBlank()) {
            productPage = sanPhamRepository.findByShop_MaShopAndTenSPContainingIgnoreCase(id, keyword.trim(), PageRequest.of(page, 10));
        } else {
            productPage = sanPhamRepository.findByShop_MaShop(id, PageRequest.of(page, 10));
        }

        model.addAttribute("shop", shop);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("keyword", keyword);
        return "admin/shop_products";
    }

    // --- KIỂM DUYỆT & KHÓA SẢN PHẨM VI PHẠM ---
    @PostMapping("/products/{id}/lock")
    public String lockProduct(@PathVariable Integer id,
                              @RequestParam(required = false) String returnUrl,
                              RedirectAttributes redirectAttributes) {
        try {
            disputeService.khoaSanPham(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa sản phẩm vi phạm #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        if (returnUrl != null && !returnUrl.isBlank()) return "redirect:" + returnUrl;
        return sanPhamRepository.findById(id)
                .filter(sp -> sp.getShop() != null)
                .map(sp -> "redirect:/admin/shops/" + sp.getShop().getMaShop() + "/products")
                .orElse("redirect:/admin/shops");
    }

    @PostMapping("/products/{id}/unlock")
    public String unlockProduct(@PathVariable Integer id,
                                @RequestParam(required = false) String returnUrl,
                                RedirectAttributes redirectAttributes) {
        try {
            disputeService.moKhoaSanPham(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa sản phẩm #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        if (returnUrl != null && !returnUrl.isBlank()) return "redirect:" + returnUrl;
        return sanPhamRepository.findById(id)
                .filter(sp -> sp.getShop() != null)
                .map(sp -> "redirect:/admin/shops/" + sp.getShop().getMaShop() + "/products")
                .orElse("redirect:/admin/shops");
    }

    // --- XỬ LÝ TRANH CHẤP TRẢ HÀNG - HOÀN TIỀN (DISPUTE RESOLUTION) ---
    @GetMapping("/disputes")
    public String disputesList(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<DonHang> disputePage = disputeService.getDisputedOrders(PageRequest.of(page, 10));
        model.addAttribute("disputes", disputePage.getContent());
        model.addAttribute("page", disputePage);
        return "admin/disputes";
    }

    @PostMapping("/disputes/{id}/arbitrate")
    public String arbitrateDispute(Authentication auth,
                                   @PathVariable Integer id,
                                   @RequestParam boolean chapNhanHoanTien,
                                   @RequestParam String phanQuyet,
                                   RedirectAttributes redirectAttributes) {
        TaiKhoan manager = getCurrentUser(auth);
        try {
            disputeService.phanQuyetTranhChap(id, chapNhanHoanTien, phanQuyet, manager);
            String verdict = chapNhanHoanTien ? "Chấp nhận hoàn tiền cho khách hàng" : "Bác bỏ khiếu nại, giữ doanh thu cho shop";
            redirectAttributes.addFlashAttribute("successMessage", "Đã phán quyết tranh chấp đơn #" + id + ": " + verdict);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/disputes";
    }

    // --- QUẢN LÝ NHÀ VẬN CHUYỂN ---
    @GetMapping("/shipping")
    public String shippingList(Model model) {
        List<NhaVanChuyen> carriers = disputeService.getAllCarriers();
        model.addAttribute("carriers", carriers);
        model.addAttribute("newCarrier", new NhaVanChuyen());
        return "admin/shipping";
    }

    @PostMapping("/shipping/save")
    public String saveCarrier(@ModelAttribute NhaVanChuyen nvc, RedirectAttributes redirectAttributes) {
        try {
            disputeService.saveCarrier(nvc);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu đối tác vận chuyển thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/shipping";
    }

    // --- CẤU HÌNH CHIẾT KHẤU SÀN (CHỈ SUPER ADMIN) ---
    @GetMapping("/platform-fees")
    public String platformFees(Model model) {
        BigDecimal defaultRate = disputeService.getPlatformCommissionRate();
        model.addAttribute("defaultRate", defaultRate);
        return "admin/platform_fees";
    }

    @PostMapping("/platform-fees/save")
    public String savePlatformFees(@RequestParam BigDecimal defaultRate, RedirectAttributes redirectAttributes) {
        try {
            disputeService.setPlatformCommissionRate(defaultRate);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tỷ lệ chiết khấu sàn mặc định thành " + defaultRate + "%");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/platform-fees";
    }
}
