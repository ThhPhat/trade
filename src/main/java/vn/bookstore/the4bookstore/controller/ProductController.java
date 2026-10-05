package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.service.DanhMucService;
import vn.bookstore.the4bookstore.service.SanPhamService;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.service.TacGiaService;
import vn.bookstore.the4bookstore.service.NhaXuatBanService;
import vn.bookstore.the4bookstore.service.UserInteractionService;
import org.springframework.security.core.Authentication;

@Controller
@RequiredArgsConstructor
@RequestMapping("/san-pham")
public class ProductController {

    private final SanPhamService sanPhamService;
    private final TacGiaService tacGiaService;
    private final NhaXuatBanService nhaXuatBanService;
    private final UserInteractionService userInteractionService;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final vn.bookstore.the4bookstore.service.ShopService shopService;

    private KhachHang getCurrentKhachHang(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        TaiKhoan tk = taiKhoanRepository.findByEmail(auth.getName())
                .or(() -> taiKhoanRepository.findByTenDangNhap(auth.getName()))
                .orElse(null);
        if (tk == null) return null;
        return khachHangRepository.findByTaiKhoan(tk).orElse(null);
    }

    // CẤP 0: Landing page — Tất cả sản phẩm (hoặc hiển thị kết quả nếu người dùng tìm kiếm / lọc)
    @GetMapping
    public String landing(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String loaiSP,
            @RequestParam(required = false) Integer danhMuc,
            @RequestParam(required = false) Integer tacGia,
            @RequestParam(required = false) Integer nxb,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // BẮT BUỘC: Phải chọn danh mục trước thì mới cho chọn loại, và loại phải thuộc danh mục đó
        if (loaiSP == null || loaiSP.isBlank() || "ALL".equalsIgnoreCase(loaiSP)) {
            danhMuc = null; // Chưa chọn danh mục cha -> không cho chọn loại
            tacGia = null;
            nxb = null;
        } else {
            // Đã chọn danh mục: kiểm tra loại có thuộc danh mục này không
            if (danhMuc != null && danhMuc > 0) {
                final Integer catId = danhMuc;
                boolean valid = sanPhamService.getCategoriesByLoaiSP(loaiSP).stream()
                        .anyMatch(c -> c.getMaDanhMuc().equals(catId));
                if (!valid) {
                    danhMuc = null; // Loại không thuộc danh mục đã chọn -> reset
                }
            }
            if (!"Sach".equalsIgnoreCase(loaiSP)) {
                tacGia = null;
                nxb = null;
            }
        }

        boolean isFiltering = (q != null && !q.isBlank()) ||
                              (loaiSP != null && !loaiSP.isBlank() && !"ALL".equalsIgnoreCase(loaiSP)) ||
                              (danhMuc != null && danhMuc > 0) ||
                              (tacGia != null && tacGia > 0) ||
                              (nxb != null && nxb > 0);

        List<DanhMuc> categoriesSach = sanPhamService.getCategoriesByLoaiSP("Sach");
        List<DanhMuc> categoriesVpp = sanPhamService.getCategoriesByLoaiSP("VanPhongPham");
        List<DanhMuc> categoriesQuaTang = sanPhamService.getCategoriesByLoaiSP("QuaTang");

        model.addAttribute("categoriesSach",    categoriesSach);
        model.addAttribute("categoriesVpp",     categoriesVpp);
        model.addAttribute("categoriesQuaTang", categoriesQuaTang);
        model.addAttribute("authors",           tacGiaService.getAll());
        model.addAttribute("publishers",        nhaXuatBanService.getAll());

        if (loaiSP != null && !loaiSP.isBlank() && !"ALL".equalsIgnoreCase(loaiSP)) {
            model.addAttribute("categories", sanPhamService.getCategoriesByLoaiSP(loaiSP));
        } else {
            model.addAttribute("categories", List.of());
        }

        if (isFiltering) {
            int pageSize = 12;
            Page<SanPham> productPage = sanPhamService.searchAndFilter(loaiSP, q, danhMuc, tacGia, nxb, sort, page, pageSize);
            List<vn.bookstore.the4bookstore.entity.Shop> matchedShops = List.of();
            if (q != null && !q.isBlank()) {
                matchedShops = shopService.searchActiveShops(q);
            }
            model.addAttribute("matchedShops",    matchedShops);
            model.addAttribute("products",        productPage.getContent());
            model.addAttribute("currentPage",     productPage.getNumber());
            model.addAttribute("totalPages",      productPage.getTotalPages());
            model.addAttribute("totalElements",   productPage.getTotalElements());
            model.addAttribute("groupName",       getGroupName(loaiSP));
            model.addAttribute("loaiSP",          loaiSP != null && !loaiSP.isBlank() ? loaiSP : "ALL");
            model.addAttribute("baseUrl",         "/san-pham");
            model.addAttribute("keyword",         q != null ? q : "");
            model.addAttribute("sortBy",          sort);
            model.addAttribute("selectedDanhMuc", danhMuc);
            model.addAttribute("selectedTacGia",  tacGia);
            model.addAttribute("selectedNxb",     nxb);
            return "products/list";
        }

        // Mặc định: hiện preview 3 Danh mục Cấp 1 kèm các loại Cấp 2
        model.addAttribute("sachPreview",       sanPhamService.getPreviewByLoaiSP("Sach"));
        model.addAttribute("vppPreview",        sanPhamService.getPreviewByLoaiSP("VanPhongPham"));
        model.addAttribute("quaTangPreview",    sanPhamService.getPreviewByLoaiSP("QuaTang"));
        return "products/index";
    }

    // CẤP 1 — Danh mục Sách
    @GetMapping("/sach")
    public String sachList(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer danhMuc,
            @RequestParam(required = false) Integer tacGia,
            @RequestParam(required = false) Integer nxb,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Integer validDanhMuc = danhMuc;
        if (validDanhMuc != null && validDanhMuc > 0) {
            final Integer checkId = validDanhMuc;
            boolean valid = sanPhamService.getCategoriesByLoaiSP("Sach").stream()
                    .anyMatch(c -> c.getMaDanhMuc().equals(checkId));
            if (!valid) validDanhMuc = null;
        }

        int pageSize = 12;
        Page<SanPham> productPage = sanPhamService.searchAndFilter("Sach", q, validDanhMuc, tacGia, nxb, sort, page, pageSize);
        List<vn.bookstore.the4bookstore.entity.Shop> matchedShops = List.of();
        if (q != null && !q.isBlank()) {
            matchedShops = shopService.searchActiveShops(q);
        }
        model.addAttribute("matchedShops",    matchedShops);
        model.addAttribute("products",        productPage.getContent());
        model.addAttribute("currentPage",     productPage.getNumber());
        model.addAttribute("totalPages",      productPage.getTotalPages());
        model.addAttribute("totalElements",   productPage.getTotalElements());
        model.addAttribute("groupName",       "Sách");
        model.addAttribute("loaiSP",          "Sach");
        model.addAttribute("baseUrl",         "/san-pham/sach");
        model.addAttribute("keyword",         q);
        model.addAttribute("sortBy",          sort);
        model.addAttribute("categories",      sanPhamService.getCategoriesByLoaiSP("Sach"));
        model.addAttribute("categoriesSach",  sanPhamService.getCategoriesByLoaiSP("Sach"));
        model.addAttribute("categoriesVpp",   sanPhamService.getCategoriesByLoaiSP("VanPhongPham"));
        model.addAttribute("categoriesQuaTang", sanPhamService.getCategoriesByLoaiSP("QuaTang"));
        model.addAttribute("authors",         tacGiaService.getAll());
        model.addAttribute("publishers",      nhaXuatBanService.getAll());
        model.addAttribute("selectedDanhMuc", validDanhMuc);
        model.addAttribute("selectedTacGia",  tacGia);
        model.addAttribute("selectedNxb",     nxb);
        return "products/list";
    }

    // CẤP 1 — Danh mục Văn phòng phẩm
    @GetMapping("/van-phong-pham")
    public String vppList(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer danhMuc,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        buildGroupModel("VanPhongPham", "Văn Phòng Phẩm", "/san-pham/van-phong-pham", q, danhMuc, sort, page, model);
        return "products/list";
    }

    // CẤP 1 — Danh mục Quà tặng & Trang trí
    @GetMapping("/qua-tang")
    public String quaTangList(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer danhMuc,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        buildGroupModel("QuaTang", "Quà Tặng & Trang Trí", "/san-pham/qua-tang", q, danhMuc, sort, page, model);
        return "products/list";
    }

    // Chi tiết sản phẩm
    @GetMapping("/{id:[0-9]+}")
    public String productDetail(@PathVariable Integer id, Authentication auth, Model model, RedirectAttributes ra) {
        Optional<SanPham> opt = sanPhamService.getById(id);
        if (opt.isEmpty()) {
            if (ra != null) ra.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại!");
            return "redirect:/san-pham";
        }
        SanPham sanPham = opt.get();
        boolean isProductLocked = "BiKhoaBoiAdmin".equalsIgnoreCase(sanPham.getTrangThaiKhoa())
                || "Khoa".equalsIgnoreCase(sanPham.getTrangThaiKhoa())
                || "NgungBan".equalsIgnoreCase(sanPham.getTrangThai());
        boolean isShopLocked = sanPham.getShop() != null && !"HoatDong".equalsIgnoreCase(sanPham.getShop().getTrangThai());

        if (isProductLocked || isShopLocked) {
            if (ra != null) ra.addFlashAttribute("errorMessage", "Sản phẩm này hiện đang tạm ngừng kinh doanh hoặc gian hàng đang bị tạm khóa!");
            return "redirect:/san-pham";
        }

        KhachHang kh = getCurrentKhachHang(auth);
        boolean isFavorite = false;
        List<Integer> eligibleOrderIds = List.of();

        if (kh != null) {
            userInteractionService.recordView(kh, id);
            isFavorite = userInteractionService.isFavorite(kh, id);
            eligibleOrderIds = userInteractionService.getEligibleOrderIdsForReview(kh, id);
        }

        List<vn.bookstore.the4bookstore.entity.DanhGia> reviews = userInteractionService.getReviewsForProduct(sanPham);
        Double avgRating = userInteractionService.getAverageRating(sanPham);

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("relatedBooks", sanPhamService.getRelatedBooks(sanPham, 4));
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("eligibleOrderIds", eligibleOrderIds);
        model.addAttribute("canReview", !eligibleOrderIds.isEmpty());
        model.addAttribute("isLoggedIn", kh != null);

        return "products/detail";
    }

    private void buildGroupModel(String loaiSP, String groupName, String baseUrl,
                                  String q, Integer maDanhMuc, String sort, int page, Model model) {
        Integer validDanhMuc = maDanhMuc;
        if (validDanhMuc != null && validDanhMuc > 0) {
            final Integer checkId = validDanhMuc;
            boolean valid = sanPhamService.getCategoriesByLoaiSP(loaiSP).stream()
                    .anyMatch(c -> c.getMaDanhMuc().equals(checkId));
            if (!valid) validDanhMuc = null;
        }

        int pageSize = 12;
        Page<SanPham> productPage = sanPhamService.searchAndFilter(loaiSP, q, validDanhMuc, sort, page, pageSize);
        List<vn.bookstore.the4bookstore.entity.Shop> matchedShops = List.of();
        if (q != null && !q.isBlank()) {
            matchedShops = shopService.searchActiveShops(q);
        }
        model.addAttribute("matchedShops",    matchedShops);
        model.addAttribute("products",        productPage.getContent());
        model.addAttribute("currentPage",     productPage.getNumber());
        model.addAttribute("totalPages",      productPage.getTotalPages());
        model.addAttribute("totalElements",   productPage.getTotalElements());
        model.addAttribute("groupName",       groupName);
        model.addAttribute("loaiSP",          loaiSP);
        model.addAttribute("baseUrl",         baseUrl);
        model.addAttribute("keyword",         q);
        model.addAttribute("sortBy",          sort);
        model.addAttribute("selectedDanhMuc", validDanhMuc);
        model.addAttribute("categories",      sanPhamService.getCategoriesByLoaiSP(loaiSP));
        model.addAttribute("categoriesSach",  sanPhamService.getCategoriesByLoaiSP("Sach"));
        model.addAttribute("categoriesVpp",   sanPhamService.getCategoriesByLoaiSP("VanPhongPham"));
        model.addAttribute("categoriesQuaTang", sanPhamService.getCategoriesByLoaiSP("QuaTang"));
        model.addAttribute("authors",         List.of());
        model.addAttribute("publishers",      List.of());
        model.addAttribute("selectedTacGia",  null);
        model.addAttribute("selectedNxb",     null);
    }

    private String getGroupName(String loaiSP) {
        if ("Sach".equalsIgnoreCase(loaiSP)) return "Sách";
        if ("VanPhongPham".equalsIgnoreCase(loaiSP)) return "Văn Phòng Phẩm";
        if ("QuaTang".equalsIgnoreCase(loaiSP)) return "Quà Tặng & Trang Trí";
        return "Tất Cả Sản Phẩm";
    }
}
