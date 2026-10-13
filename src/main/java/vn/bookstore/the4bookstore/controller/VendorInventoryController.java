package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import vn.bookstore.the4bookstore.service.ShopService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@Controller
@RequestMapping("/vendor/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'MANAGER')")
public class VendorInventoryController {

    private final ShopService shopService;
    private final SanPhamRepository sanPhamRepository;
    private final PhieuKiemKeRepository phieuKiemKeRepository;
    private final ChiTietKiemKeRepository chiTietKiemKeRepository;
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

    // --- 1. DANH SÁCH BIÊN BẢN KIỂM KÊ CỦA VENDOR ---
    @GetMapping
    public String listInventoryAudits(@RequestParam(defaultValue = "0") int page,
                                     Authentication auth,
                                     Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        Page<PhieuKiemKe> auditPage = phieuKiemKeRepository.findByShop_MaShopOrderByNgayKiemKeDesc(shop.getMaShop(), PageRequest.of(page, 10));
        long totalProducts = sanPhamRepository.countByShop_MaShop(shop.getMaShop());

        model.addAttribute("shop", shop);
        model.addAttribute("auditPage", auditPage);
        model.addAttribute("audits", auditPage.getContent());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("currentPage", page);
        return "vendor/inventory_list";
    }

    // --- 2. TRANG LẬP PHIẾU KIỂM KÊ MỚI CỦA SHOP ---
    @GetMapping("/create")
    public String showCreateAuditForm(Authentication auth, Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        List<SanPham> products = sanPhamRepository.findByShop_MaShop(shop.getMaShop());

        model.addAttribute("shop", shop);
        model.addAttribute("products", products);
        return "vendor/inventory_form";
    }

    // --- 3. LƯU & CÂN BẰNG TỒN KHO CHO GIAN HÀNG ---
    @PostMapping("/create")
    @Transactional
    public String submitAudit(@RequestParam(required = false) String ghiChu,
                              @RequestParam("productIds") List<Integer> productIds,
                              @RequestParam("soLuongThucTes") List<Integer> soLuongThucTes,
                              @RequestParam(value = "lyDos", required = false) List<String> lyDos,
                              Authentication auth,
                              RedirectAttributes ra) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        try {
            if (productIds == null || productIds.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Không có sản phẩm nào được chọn để kiểm kê!");
                return "redirect:/vendor/inventory/create";
            }

            // Tạo phiếu kiểm kê cho Shop
            PhieuKiemKe pkk = new PhieuKiemKe();
            pkk.setShop(shop);
            pkk.setNgayKiemKe(LocalDateTime.now());
            pkk.setNgayPheDuyet(LocalDateTime.now());
            pkk.setTrangThai("DaCanHang"); // Vendor tự chủ kiểm kê và cân bằng ngay
            pkk.setGhiChu(ghiChu != null && !ghiChu.isBlank() ? ghiChu.trim() : "Biên bản kiểm kê định kỳ gian hàng");
            pkk = phieuKiemKeRepository.save(pkk);

            List<ChiTietKiemKe> chiTiets = new ArrayList<>();
            int countUpdated = 0;

            for (int i = 0; i < productIds.size(); i++) {
                Integer pid = productIds.get(i);
                Integer actualQty = (soLuongThucTes != null && i < soLuongThucTes.size()) ? soLuongThucTes.get(i) : null;
                String reason = (lyDos != null && i < lyDos.size()) ? lyDos.get(i) : "";

                if (pid != null && actualQty != null && actualQty >= 0) {
                    SanPham sp = sanPhamRepository.findById(pid).orElse(null);
                    if (sp != null && sp.getShop() != null && sp.getShop().getMaShop().equals(shop.getMaShop())) {
                        int systemQty = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;

                        ChiTietKiemKe ct = new ChiTietKiemKe();
                        ct.setPhieuKiemKe(pkk);
                        ct.setSanPham(sp);
                        ct.setSoLuongHeThong(systemQty);
                        ct.setSoLuongThucTe(actualQty);
                        ct.setLyDo(reason != null && !reason.isBlank() ? reason.trim() : (actualQty != systemQty ? "Cân bằng theo kiểm đếm thực tế" : "Khớp số lượng"));
                        chiTiets.add(ct);

                        // Cân bằng trực tiếp vào số lượng tồn của sản phẩm thuộc Shop
                        sp.setSoLuongTon(actualQty);
                        sanPhamRepository.save(sp);
                        countUpdated++;
                    }
                }
            }

            if (!chiTiets.isEmpty()) {
                chiTietKiemKeRepository.saveAll(chiTiets);
            }

            ra.addFlashAttribute("successMessage", "Lập phiếu kiểm kê #" + pkk.getMaPhieuKiemKe() + " và cân bằng " + countUpdated + " sản phẩm thành công!");
            return "redirect:/vendor/inventory";
        } catch (Exception e) {
            log.error("Loi lap phieu kiem ke vendor: ", e);
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/vendor/inventory/create";
        }
    }

    // --- 4. XEM CHI TIẾT BIÊN BẢN KIỂM KÊ ---
    @GetMapping("/{id}")
    public String viewAuditDetail(@PathVariable Integer id,
                                  Authentication auth,
                                  Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        PhieuKiemKe pkk = phieuKiemKeRepository.findById(id).orElse(null);
        if (pkk == null || pkk.getShop() == null || !pkk.getShop().getMaShop().equals(shop.getMaShop())) {
            return "redirect:/vendor/inventory?error=notFound";
        }

        List<ChiTietKiemKe> chiTiets = chiTietKiemKeRepository.findByPhieuKiemKe(pkk);

        model.addAttribute("shop", shop);
        model.addAttribute("audit", pkk);
        model.addAttribute("chiTiets", chiTiets);
        return "vendor/inventory_detail";
    }
}
