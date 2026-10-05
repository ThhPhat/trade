package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;
import org.springframework.web.multipart.MultipartFile;
import vn.bookstore.the4bookstore.service.CloudinaryService;
import vn.bookstore.the4bookstore.service.ShopService;
import vn.bookstore.the4bookstore.service.VendorService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final ShopService shopService;
    private final VendorService vendorService;
    private final CloudinaryService cloudinaryService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DanhMucRepository danhMucRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;

    private TaiKhoan getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        TaiKhoan tk = null;
        Object p = auth.getPrincipal();
        if (p instanceof CustomUserDetails ud && ud.getTaiKhoan() != null && ud.getTaiKhoan().getMaTaiKhoan() != null) {
            tk = taiKhoanRepository.findById(ud.getTaiKhoan().getMaTaiKhoan()).orElse(ud.getTaiKhoan());
        } else if (p instanceof CustomOAuth2User o && o.getEmail() != null) {
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        } else if (p instanceof CustomOidcUser o && o.getEmail() != null) {
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        } else if (p instanceof OidcUser o && o.getEmail() != null) {
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        } else if (p instanceof OAuth2User o) {
            Object em = o.getAttribute("email");
            if (em != null) tk = taiKhoanRepository.findByEmail(em.toString()).orElse(null);
        }
        if (tk == null && auth.getName() != null) {
            tk = taiKhoanRepository.findByEmail(auth.getName())
                    .or(() -> taiKhoanRepository.findByTenDangNhap(auth.getName()))
                    .orElse(null);
        }
        return tk;
    }

    private String checkShopAccess(Shop shop) {
        if (shop == null) return "redirect:/vendor/register";
        if ("ChoDuyet".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/pending";
        }
        if ("BiKhoa".equalsIgnoreCase(shop.getTrangThai()) || "DaXoa".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/blocked";
        }
        return null;
    }

    @GetMapping
    public String index(Authentication auth) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        return "redirect:/vendor/dashboard";
    }

    private Shop getCurrentShop(Authentication auth) {
        TaiKhoan user = getCurrentUser(auth);
        if (user == null) return null;
        return shopService.findByTaiKhoan(user).orElse(null);
    }

    // --- TRẠNG THÁI HỒ SƠ CHỜ DUYỆT ---
    @GetMapping("/pending")
    public String pendingApproval(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";
        if ("HoatDong".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/dashboard";
        }
        if ("BiKhoa".equalsIgnoreCase(shop.getTrangThai()) || "DaXoa".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/blocked";
        }
        model.addAttribute("shop", shop);
        return "vendor/pending";
    }

    // --- TRẠNG THÁI GIAN HÀNG TẠM KHÓA ---
    @GetMapping("/blocked")
    public String blockedNotice(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";
        if ("HoatDong".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/dashboard";
        }
        if ("ChoDuyet".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/pending";
        }
        model.addAttribute("shop", shop);
        return "vendor/blocked";
    }

    // --- ĐĂNG KÝ MỞ SHOP ---
    @GetMapping("/register")
    public String registerForm(Authentication auth, Model model) {
        TaiKhoan user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        Optional<Shop> existing = shopService.findByTaiKhoan(user);
        if (existing.isPresent()) {
            Shop s = existing.get();
            if ("ChoDuyet".equalsIgnoreCase(s.getTrangThai())) return "redirect:/vendor/pending";
            if ("BiKhoa".equalsIgnoreCase(s.getTrangThai()) || "DaXoa".equalsIgnoreCase(s.getTrangThai())) return "redirect:/vendor/blocked";
            return "redirect:/vendor/dashboard";
        }
        return "vendor/register";
    }

    @PostMapping("/register")
    public String handleRegister(Authentication auth,
                                 @RequestParam String tenShop,
                                 @RequestParam(required = false) String moTa,
                                 @RequestParam String diaChiShop,
                                 @RequestParam String soDienThoai,
                                 @RequestParam String emailShop,
                                 RedirectAttributes redirectAttributes) {
        TaiKhoan user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        try {
            shopService.registerShop(user, tenShop, moTa, diaChiShop, soDienThoai, emailShop);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký mở gian hàng thành công! Hồ sơ của bạn đã được gửi và đang chờ Ban Quản Trị phê duyệt.");
            return "redirect:/vendor/pending";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/vendor/register";
        }
    }

    // --- DASHBOARD KÊNH NGƯỜI BÁN ---
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        Map<String, Object> stats = vendorService.getShopDashboardStats(shop.getMaShop());
        model.addAttribute("shop", shop);
        model.addAttribute("stats", stats);
        return "vendor/dashboard";
    }

    // --- HỒ SƠ & TRANG TRÍ SHOP ---
    @GetMapping("/profile")
    public String shopProfile(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        model.addAttribute("shop", shop);
        return "vendor/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication auth,
                                @RequestParam String tenShop,
                                @RequestParam(required = false) String moTa,
                                @RequestParam String diaChiShop,
                                @RequestParam String soDienThoai,
                                @RequestParam String emailShop,
                                @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
                                @RequestParam(required = false) String logo,
                                @RequestParam(required = false) String banner,
                                RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String uploadedLogo = cloudinaryService.uploadImage(logoFile, "shops");
                if (uploadedLogo != null && !uploadedLogo.isBlank()) {
                    logo = uploadedLogo;
                }
            }
            if (bannerFile != null && !bannerFile.isEmpty()) {
                String uploadedBanner = cloudinaryService.uploadImage(bannerFile, "shops");
                if (uploadedBanner != null && !uploadedBanner.isBlank()) {
                    banner = uploadedBanner;
                }
            }
            shopService.updateShopProfile(shop.getMaShop(), tenShop, moTa, diaChiShop, soDienThoai, emailShop, logo, banner);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ gian hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/profile";
    }

    // --- QUẢN LÝ SẢN PHẨM CỦA SHOP ---
    @GetMapping("/products")
    public String products(Authentication auth,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        Page<SanPham> productPage = vendorService.getShopProducts(shop.getMaShop(), keyword, PageRequest.of(page, 10));
        model.addAttribute("shop", shop);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("keyword", keyword);
        return "vendor/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        model.addAttribute("shop", shop);
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("nhaXuatBans", nhaXuatBanRepository.findAll());
        return "vendor/product_form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(Authentication auth, @PathVariable Integer id, Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        SanPham sp = vendorService.getShopProducts(shop.getMaShop(), null, PageRequest.of(0, 1000))
                .stream().filter(p -> p.getMaSP().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm hoặc bạn không có quyền sửa"));

        model.addAttribute("shop", shop);
        model.addAttribute("sanPham", sp);
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("nhaXuatBans", nhaXuatBanRepository.findAll());
        return "vendor/product_form";
    }

    @PostMapping("/products/save")
    public String saveProduct(Authentication auth,
                              @ModelAttribute SanPham sanPham,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(required = false) Integer maDanhMuc,
                              @RequestParam(required = false) Integer maNXB,
                              RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadedImg = cloudinaryService.uploadImage(imageFile, "books");
                if (uploadedImg != null && !uploadedImg.isBlank()) {
                    sanPham.setHinhAnh(uploadedImg);
                }
            }
            vendorService.saveShopProduct(shop.getMaShop(), sanPham, maDanhMuc, maNXB);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        try {
            vendorService.deleteShopProduct(shop.getMaShop(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ngừng bán sản phẩm!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/products";
    }

    // --- QUẢN LÝ ĐƠN HÀNG CỦA SHOP (7 TRẠNG THÁI) ---
    @GetMapping("/orders")
    public String orders(Authentication auth,
                         @RequestParam(required = false, defaultValue = "ALL") String status,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        Page<DonHang> orderPage = vendorService.getShopOrders(shop.getMaShop(), status, PageRequest.of(page, 10));
        model.addAttribute("shop", shop);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("page", orderPage);
        model.addAttribute("currentStatus", status);
        return "vendor/orders";
    }

    @PostMapping("/orders/{id}/confirm")
    public String confirmOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.xacNhanDonHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/pickup")
    public String pickupOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.daLayHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đã bàn giao đơn hàng #" + id + " cho đơn vị vận chuyển.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/delivering")
    public String deliveringOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.dangGiaoHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng #" + id + " đang trên đường giao.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/complete")
    public String completeOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.daGiaoHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng #" + id + " đã giao thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(Authentication auth, @PathVariable Integer id, @RequestParam(required = false) String lyDo, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.huyDonHang(id, shop.getMaShop(), lyDo != null ? lyDo : "Shop hết hàng");
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/accept-return")
    public String acceptReturn(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.dongYTraHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đã đồng ý trả hàng - hoàn tiền cho đơn #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/reject-return")
    public String rejectReturn(Authentication auth, @PathVariable Integer id, @RequestParam String lyDo, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.tuChoiTraHang(id, shop.getMaShop(), lyDo);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối trả hàng cho đơn #" + id + ". Đơn đã chuyển sang trạng thái Tranh Chấp để Quản Lý xem xét.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    // --- KHUYẾN MÃI CỦA SHOP ---
    @GetMapping("/promotions")
    public String promotions(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        model.addAttribute("shop", shop);
        model.addAttribute("promotions", vendorService.getShopPromotions(shop.getMaShop()));
        return "vendor/promotions";
    }

    @PostMapping("/promotions/create")
    public String createPromotion(Authentication auth,
                                  @ModelAttribute KhuyenMai km,
                                  RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        try {
            vendorService.createShopPromotion(shop.getMaShop(), km);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã khuyến mãi của shop thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/promotions";
    }
}
