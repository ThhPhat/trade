package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.KhuyenMaiRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.repository.VoucherDaLuuRepository;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import vn.bookstore.the4bookstore.service.DanhMucService;
import vn.bookstore.the4bookstore.service.SanPhamService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SanPhamService sanPhamService;
    private final DanhMucService danhMucService;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final VoucherDaLuuRepository voucherDaLuuRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    private KhachHang getCurrentKhachHang(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        TaiKhoan tk = null;
        Object p = auth.getPrincipal();
        if (p instanceof CustomUserDetails ud && ud.getTaiKhoan() != null && ud.getTaiKhoan().getMaTaiKhoan() != null)
            tk = taiKhoanRepository.findById(ud.getTaiKhoan().getMaTaiKhoan()).orElse(ud.getTaiKhoan());
        else if (p instanceof CustomOAuth2User o && o.getEmail() != null)
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        else if (p instanceof CustomOidcUser o && o.getEmail() != null)
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        else if (p instanceof OidcUser o && o.getEmail() != null)
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        else if (p instanceof OAuth2User o) {
            Object em = o.getAttribute("email");
            if (em != null) tk = taiKhoanRepository.findByEmail(em.toString()).orElse(null);
        }
        if (tk == null && auth.getName() != null)
            tk = taiKhoanRepository.findByEmail(auth.getName()).or(() -> taiKhoanRepository.findByTenDangNhap(auth.getName())).orElse(null);
        if (tk == null) return null;
        return khachHangRepository.findByTaiKhoan(tk).orElse(null);
    }

    @GetMapping("/")
    public String home(
            @RequestParam(name = "catPage", required = false) Integer catPage,
            @RequestParam(name = "page", required = false) Integer page,
            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("username", auth.getName());
        }

        int requestedPage = (catPage != null) ? catPage : (page != null ? page : 0);
        if (requestedPage < 0) requestedPage = 0;

        int pageSize = 10;
        List<DanhMuc> allActive = danhMucService.getAllActive();
        Page<DanhMuc> categoryPage = danhMucService.getAllActivePaged(requestedPage, pageSize);

        // Section: Sách Bán Chạy
        model.addAttribute("featuredBooks", sanPhamService.getFeaturedBooks());

        // Section: Tất Cả Sản Phẩm (Previews & Subcategories)
        model.addAttribute("sachPreview", sanPhamService.getPreviewByLoaiSP("Sach"));
        model.addAttribute("vppPreview", sanPhamService.getPreviewByLoaiSP("VanPhongPham"));
        model.addAttribute("quaTangPreview", sanPhamService.getPreviewByLoaiSP("QuaTang"));
        model.addAttribute("categoriesSach", sanPhamService.getCategoriesByLoaiSP("Sach"));
        model.addAttribute("categoriesVpp", sanPhamService.getCategoriesByLoaiSP("VanPhongPham"));
        model.addAttribute("categoriesQuaTang", sanPhamService.getCategoriesByLoaiSP("QuaTang"));

        // Section: Các Danh Mục (ở cuối, 10 mục / trang)
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("allCategories", allActive);
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("catCurrentPage", categoryPage.getNumber());
        model.addAttribute("catTotalPages", categoryPage.getTotalPages());
        model.addAttribute("catTotalElements", categoryPage.getTotalElements());

        // Section: Săn Voucher - Lấy voucher công khai đang hoạt động
        List<KhuyenMai> publicVouchers = Collections.emptyList();
        Set<Integer> savedVoucherIds = new HashSet<>();
        try {
            publicVouchers = khuyenMaiRepository.findAll().stream()
                    .filter(km -> "HoatDong".equalsIgnoreCase(km.getTrangThai()))
                    .filter(km -> Boolean.TRUE.equals(km.getHienThiCongKhai()))
                    .filter(KhuyenMai::isDangDienRa)
                    .toList();

            KhachHang kh = getCurrentKhachHang(auth);
            if (kh != null) {
                savedVoucherIds.addAll(voucherDaLuuRepository.findSavedVoucherIdsByKhachHang(kh));
            }
        } catch (Exception ignored) {}
        model.addAttribute("publicVouchers", publicVouchers);
        model.addAttribute("savedVoucherIds", savedVoucherIds);

        return "home/index";
    }
}
