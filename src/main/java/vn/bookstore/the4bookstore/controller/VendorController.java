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

    @GetMapping
    public String index(Authentication auth) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";
        return "redirect:/vendor/dashboard";
    }

    private Shop getCurrentShop(Authentication auth) {
        TaiKhoan user = getCurrentUser(auth);
        if (user == null) return null;
        return shopService.findByTaiKhoan(user).orElse(null);
    }

    // --- ĐĂNG KÝ MỞ SHOP ---
    @GetMapping("/register")
    public String registerForm(Authentication auth, Model model) {
        TaiKhoan user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        Optional<Shop> existing = shopService.findByTaiKhoan(user);
        if (existing.isPresent()) {
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

            // Nâng cấp quyền ROLE_VENDOR ngay trong SecurityContext của session hiện tại
            List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
            if (auth.getAuthorities() != null) {
                updatedAuthorities.addAll(auth.getAuthorities());
            }
            if (updatedAuthorities.stream().noneMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_VENDOR"))) {
                updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_VENDOR"));
            }
            user.setVaiTro("VENDOR");
            CustomUserDetails updatedUserDetails = new CustomUserDetails(user);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    auth.getCredentials(),
                    updatedAuthorities
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký mở gian hàng thành công! Chào mừng bạn đến với Kênh Người Bán.");
            return "redirect:/vendor/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/vendor/register";
        }
    }

    // --- DASHBOARD KÊNH NGƯỜI BÁN ---
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";

        Map<String, Object> stats = vendorService.getShopDashboardStats(shop.getMaShop());
        model.addAttribute("shop", shop);
        model.addAttribute("stats", stats);
        return "vendor/dashboard";
    }

    // --- HỒ SƠ & TRANG TRÍ SHOP ---
    @GetMapping("/profile")
    public String shopProfile(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";
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
                                @RequestParam(required = false) String logo,
                                @RequestParam(required = false) String banner,
                                RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";

        try {
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
        if (shop == null) return "redirect:/vendor/register";

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
        if (shop == null) return "redirect:/vendor/register";

        model.addAttribute("shop", shop);
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("nhaXuatBans", nhaXuatBanRepository.findAll());
        return "vendor/product_form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(Authentication auth, @PathVariable Integer id, Model model) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";

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
                              @RequestParam(required = false) Integer maDanhMuc,
                              @RequestParam(required = false) Integer maNXB,
                              RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";

        try {
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
        if (shop == null) return "redirect:/vendor/register";

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
        if (shop == null) return "redirect:/vendor/register";

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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";
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
        if (shop == null) return "redirect:/vendor/register";

        model.addAttribute("shop", shop);
        model.addAttribute("promotions", vendorService.getShopPromotions(shop.getMaShop()));
        return "vendor/promotions";
    }

    @PostMapping("/promotions/create")
    public String createPromotion(Authentication auth,
                                  @ModelAttribute KhuyenMai km,
                                  RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";

        try {
            vendorService.createShopPromotion(shop.getMaShop(), km);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã khuyến mãi của shop thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/promotions";
    }
}
