package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.bookstore.the4bookstore.dto.VendorImportRequest;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import vn.bookstore.the4bookstore.service.ShopService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/vendor/imports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'MANAGER')")
public class VendorImportController {

    private final ShopService shopService;
    private final SanPhamRepository sanPhamRepository;
    private final PhieuNhapRepository phieuNhapRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;
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

    // --- 1. DANH SÁCH PHIẾU NHẬP HÀNG CỦA VENDOR ---
    @GetMapping
    public String listImports(@RequestParam(defaultValue = "0") int page,
                              Authentication auth,
                              Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        Page<PhieuNhap> importPage = phieuNhapRepository.findByShop_MaShopOrderByNgayNhapDesc(shop.getMaShop(), PageRequest.of(page, 10));
        long totalProducts = sanPhamRepository.countByShop_MaShop(shop.getMaShop());

        model.addAttribute("shop", shop);
        model.addAttribute("importPage", importPage);
        model.addAttribute("imports", importPage.getContent());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("currentPage", page);
        return "vendor/import_list";
    }

    // --- 2. TRANG LẬP PHIẾU NHẬP HÀNG MỚI CỦA SHOP ---
    @GetMapping("/create")
    public String showCreateImportForm(Authentication auth, Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        List<SanPham> products = sanPhamRepository.findByShop_MaShop(shop.getMaShop());

        model.addAttribute("shop", shop);
        model.addAttribute("products", products);
        return "vendor/import_form";
    }

    // --- 3. LƯU & XÁC NHẬN PHIẾU NHẬP HÀNG (TỰ ĐỘNG CỘNG TỒN KHO) ---
    @PostMapping
    @ResponseBody
    @Transactional
    public ResponseEntity<?> submitImport(@RequestBody VendorImportRequest request, Authentication auth) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Vui lòng đăng nhập!"));
        }

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "error", "Bạn chưa đăng ký gian hàng!"));
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Phiếu nhập phải có ít nhất 1 sản phẩm!"));
        }

        // Tạo PhieuNhap gắn với Shop
        PhieuNhap pn = new PhieuNhap();
        pn.setShop(shop);
        pn.setNhaCungCapTen(request.getNhaCungCapTen() != null && !request.getNhaCungCapTen().isBlank() 
                ? request.getNhaCungCapTen().trim() 
                : "Đối tác cung ứng riêng của Shop");
        pn.setGhiChu(request.getGhiChu());
        pn.setNgayNhap(LocalDateTime.now());
        pn.setTrangThai("DaNhap");
        pn.setTongTien(0);

        List<ChiTietPhieuNhap> details = new ArrayList<>();
        int totalAmount = 0;

        for (VendorImportRequest.Item item : request.getItems()) {
            if (item.getMaSP() == null || item.getSoLuong() == null || item.getSoLuong() <= 0) {
                continue;
            }
            SanPham sp = sanPhamRepository.findById(item.getMaSP()).orElse(null);
            if (sp == null || sp.getShop() == null || !sp.getShop().getMaShop().equals(shop.getMaShop())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Sản phẩm mã #" + item.getMaSP() + " không thuộc quyền quản lý của gian hàng bạn!"
                ));
            }

            int donGiaNhap = item.getDonGiaNhap() != null && item.getDonGiaNhap() >= 0 ? item.getDonGiaNhap() : 0;
            totalAmount += item.getSoLuong() * donGiaNhap;

            // Chi tiết phiếu nhập
            ChiTietPhieuNhap ctpn = new ChiTietPhieuNhap();
            ctpn.setPhieuNhap(pn);
            ctpn.setSanPham(sp);
            ctpn.setSoLuong(item.getSoLuong());
            ctpn.setDonGiaNhap(donGiaNhap);
            details.add(ctpn);

            // Tự động CỘNG DỒN số lượng tồn kho của sách
            int currentStock = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;
            sp.setSoLuongTon(currentStock + item.getSoLuong());
            sanPhamRepository.save(sp);
        }

        if (details.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Không có sản phẩm hợp lệ nào trong danh sách nhập!"));
        }

        pn.setTongTien(totalAmount);
        pn.setChiTietPhieuNhaps(details);
        PhieuNhap saved = phieuNhapRepository.save(pn);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "id", saved.getMaPN(),
                "message", "Nhập hàng thành công! Đã tự động cộng dồn tồn kho cho " + details.size() + " sản phẩm."
        ));
    }

    // --- 4. CHI TIẾT PHIẾU NHẬP HÀNG ---
    @GetMapping("/{id}")
    public String viewImportDetail(@PathVariable Integer id, Authentication auth, Model model) {
        TaiKhoan tk = getCurrentUser(auth);
        if (tk == null) return "redirect:/login";

        Shop shop = shopService.findByTaiKhoan(tk).orElse(null);
        if (shop == null) return "redirect:/vendor/register";

        PhieuNhap pn = phieuNhapRepository.findById(id).orElse(null);
        if (pn == null || pn.getShop() == null || !pn.getShop().getMaShop().equals(shop.getMaShop())) {
            return "redirect:/vendor/imports";
        }

        List<ChiTietPhieuNhap> details = chiTietPhieuNhapRepository.findByPhieuNhap(pn);

        model.addAttribute("shop", shop);
        model.addAttribute("importOrder", pn);
        model.addAttribute("details", details);
        return "vendor/import_detail";
    }
}
