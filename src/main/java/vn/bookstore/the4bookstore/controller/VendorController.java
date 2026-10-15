package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
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
    private final TacGiaRepository tacGiaRepository;
    private final SanPhamRepository sanPhamRepository;

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
        } else if (p instanceof CustomOidcUser o && o.getEmail() != null) {
            tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        } else if (p instanceof OidcUser o) {
            String sub = o.getSubject();
            if (sub != null) tk = taiKhoanRepository.findByProviderId(sub).orElse(null);
            if (tk == null && o.getEmail() != null) tk = taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        } else if (p instanceof OAuth2User o) {
            Object sub = o.getAttribute("sub");
            if (sub != null) tk = taiKhoanRepository.findByProviderId(sub.toString()).orElse(null);
            if (tk == null) {
                Object em = o.getAttribute("email");
                if (em != null) tk = taiKhoanRepository.findByEmail(em.toString()).orElse(null);
            }
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
        if ("BiKhoa".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/blocked";
        }
        if ("DaXoa".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/register";
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
        Optional<Shop> userShop = shopService.findByTaiKhoan(user);
        if (userShop.isPresent()) {
            return userShop.get();
        }
        // Nếu user có vai trò ADMIN, MANAGER hoặc QUANLY mà chưa có shop riêng, tự động liên kết với Shop 1 (The4BookStore Official)
        if ("ADMIN".equalsIgnoreCase(user.getVaiTro()) || "MANAGER".equalsIgnoreCase(user.getVaiTro()) || "QUANLY".equalsIgnoreCase(user.getVaiTro())) {
            return shopService.findById(1).orElse(null);
        }
        return null;
    }

    // --- TRẠNG THÁI HỒ SƠ CHỜ DUYỆT ---
    @GetMapping("/pending")
    public String pendingApproval(Authentication auth, Model model) {
        Shop shop = getCurrentShop(auth);
        if (shop == null) return "redirect:/vendor/register";
        if ("HoatDong".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/vendor/dashboard";
        }
        if ("BiKhoa".equalsIgnoreCase(shop.getTrangThai())) {
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
            if ("BiKhoa".equalsIgnoreCase(s.getTrangThai())) return "redirect:/vendor/blocked";
            if (!"DaXoa".equalsIgnoreCase(s.getTrangThai())) {
                return "redirect:/vendor/dashboard";
            }
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

        Page<SanPham> productPage = vendorService.getShopProducts(shop.getMaShop(), keyword, 
                PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "maSP")));
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
        model.addAttribute("tacGias", tacGiaRepository.findAll());
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
        model.addAttribute("tacGias", tacGiaRepository.findAll());
        return "vendor/product_form";
    }

    @PostMapping("/products/save")
    public String saveProduct(Authentication auth,
                              @ModelAttribute SanPham sanPham,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(required = false) Integer maDanhMuc,
                              @RequestParam(required = false) Integer maNXB,
                              @RequestParam(required = false) String tenNXB,
                              @RequestParam(required = false) String tenTacGia,
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
            vendorService.saveShopProduct(shop.getMaShop(), sanPham, maDanhMuc, maNXB, tenNXB, tenTacGia);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm thành công!");
            return "redirect:/vendor/products";
        } catch (Exception ex) {
            log.error("Lỗi khi lưu sản phẩm cho shop #{}: ", shop.getMaShop(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu sách: " + ex.getMessage());
            if (sanPham.getMaSP() != null) {
                return "redirect:/vendor/products/edit/" + sanPham.getMaSP();
            }
            return "redirect:/vendor/products/add";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        try {
            vendorService.deleteShopProduct(shop.getMaShop(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + ex.getMessage());
        }
        return "redirect:/vendor/products";
    }

    // ==================== TẢI FILE CSV MẪU NHẬP SÁCH CHO GIAN HÀNG ====================
    @GetMapping("/products/sample-csv")
    public void downloadSampleProductsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"mau_nhap_sach_shop.csv\"");

        try (OutputStream os = response.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            os.write(0xEF);
            os.write(0xBB);
            os.write(0xBF);

            writer.println("ISBN,TenSP,GiaBan,SoLuongTon,LoaiSP,MaDanhMuc,MaNXB,MoTa");
            writer.println("9786043658999,Đắc Nhân Tâm - Khổ Tiêu Chuẩn,85000,100,Sach,1,1,Sách kinh điển về nghệ thuật ứng xử và kết nối.");
            writer.println("8935244855523,Sổ Tay Dot Grid Bìa Da Cao Cấp,45000,50,VanPhongPham,2,1,Sổ tay kẻ chấm dot ghi chép bullet journal.");
            writer.flush();
        }
    }

    // ==================== VENDOR NHẬP SẢN PHẨM HÀNG LOẠT TỪ FILE CSV ====================
    @PostMapping("/products/import-csv")
    public String importProductsCsv(Authentication auth,
                                    @RequestParam("file") MultipartFile file,
                                    RedirectAttributes ra) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;

        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng chọn file CSV để tải lên!");
            return "redirect:/vendor/products";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            ra.addFlashAttribute("errorMessage", "Định dạng file không hợp lệ! Vui lòng tải lên file có đuôi .csv");
            return "redirect:/vendor/products";
        }

        int addedCount = 0;
        int updatedCount = 0;
        int rowIdx = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                rowIdx++;
                if (rowIdx == 1) continue; // Bỏ qua header
                if (line.trim().isEmpty()) continue;

                String[] cols = parseCsvLine(line);
                if (cols.length < 4) continue;

                String isbn = cols[0].trim();
                String tenSP = cols[1].trim();
                int giaBan = parseIntSafe(cols[2], 0);
                int tonKho = parseIntSafe(cols[3], 0);
                String loaiSP = cols.length > 4 && !cols[4].isBlank() ? cols[4].trim() : "Sach";
                Integer maDanhMuc = cols.length > 5 ? parseIntegerSafe(cols[5]) : 1;
                Integer maNxb = cols.length > 6 ? parseIntegerSafe(cols[6]) : null;
                String moTa = cols.length > 7 ? cols[7].trim() : "";

                if (tenSP.isEmpty()) continue;

                // Kiểm tra xem sách này đã có trong shop chưa
                Optional<SanPham> existingOpt = Optional.empty();
                if (!isbn.isEmpty()) {
                    existingOpt = sanPhamRepository.findByISBNAndShop_MaShop(isbn, shop.getMaShop());
                }

                if (existingOpt.isPresent()) {
                    // Cập nhật sách đã có của shop
                    SanPham sp = existingOpt.get();
                    sp.setTenSP(tenSP);
                    sp.setGiaBan(giaBan);
                    int curStock = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;
                    sp.setSoLuongTon(curStock + tonKho);
                    if (!moTa.isEmpty()) sp.setMoTa(moTa);
                    sanPhamRepository.save(sp);
                    updatedCount++;
                } else {
                    // Thêm mới sản phẩm vào gian hàng của Vendor
                    SanPham sp = new SanPham();
                    sp.setISBN(!isbn.isEmpty() ? isbn : "ISBN-" + System.currentTimeMillis() % 1000000000L);
                    sp.setTenSP(tenSP);
                    sp.setGiaBan(giaBan);
                    sp.setSoLuongTon(tonKho);
                    sp.setSoLuongDaBan(0);
                    sp.setMucTonToiThieu(5);
                    sp.setLoaiSP(loaiSP);
                    sp.setMoTa(moTa);
                    sp.setTrangThai("DangBan");
                    sp.setTrangThaiKhoa("BinhThuong");
                    sp.setNgayTao(LocalDateTime.now());
                    sp.setShop(shop); // Gắn đúng vào Shop của Vendor

                    if (maDanhMuc != null) {
                        danhMucRepository.findById(maDanhMuc).ifPresent(sp::setDanhMuc);
                    }
                    if (sp.getDanhMuc() == null) {
                        danhMucRepository.findAll().stream().findFirst().ifPresent(sp::setDanhMuc);
                    }
                    if (maNxb != null) {
                        nhaXuatBanRepository.findById(maNxb).ifPresent(sp::setNhaXuatBan);
                    }

                    sanPhamRepository.save(sp);
                    addedCount++;
                }
            }

            ra.addFlashAttribute("successMessage",
                    String.format("Xử lý file CSV thành công: Thêm mới %d sản phẩm, Cập nhật tồn kho %d sản phẩm vào gian hàng của bạn!", addedCount, updatedCount));
        } catch (Exception ex) {
            log.error("Lỗi khi nhập CSV cho shop #{}: ", shop.getMaShop(), ex);
            ra.addFlashAttribute("errorMessage", "Lỗi xử lý file CSV tại dòng " + rowIdx + ": " + ex.getMessage());
        }

        return "redirect:/vendor/products";
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private int parseIntSafe(String val, int def) {
        try {
            return Integer.parseInt(val.trim().replace(".", "").replace(",", ""));
        } catch (Exception e) {
            return def;
        }
    }

    private Integer parseIntegerSafe(String val) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return null;
        }
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

    @PostMapping("/orders/{id}/confirm-payment")
    public String confirmPayment(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.xacNhanThanhToan(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán thành công cho đơn hàng #" + id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
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

    @PostMapping({"/orders/{id}/delivered", "/orders/{id}/complete"})
    public String deliveredOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.daGiaoHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng #" + id + " đã giao thành công! Kích hoạt thời gian chờ khiếu nại (tiền trong Escrow).");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/finalize")
    public String finalizeOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.hoanTatDonHang(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đã quyết toán và hoàn tất đơn hàng #" + id + "! Doanh thu đã được cộng vào số dư shop.");
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
            vendorService.huyDonHang(id, shop.getMaShop(), lyDo != null ? lyDo : "Shop hết hàng đột xuất");
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " và hoàn trả lại số lượng tồn kho.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/accept-cancel")
    public String acceptCancelOrder(Authentication auth, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.chapNhanHuyDon(id, shop.getMaShop());
            redirectAttributes.addFlashAttribute("successMessage", "Đã đồng ý yêu cầu hủy đơn hàng #" + id + ". Tồn kho đã được hoàn trả!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    @PostMapping("/orders/{id}/reject-cancel")
    public String rejectCancelOrder(Authentication auth, @PathVariable Integer id, @RequestParam(required = false) String lyDo, RedirectAttributes redirectAttributes) {
        Shop shop = getCurrentShop(auth);
        String redirect = checkShopAccess(shop);
        if (redirect != null) return redirect;
        try {
            vendorService.tuChoiHuyDon(id, shop.getMaShop(), lyDo);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu hủy cho đơn #" + id + ". Đơn hàng tiếp tục trong quy trình giao!");
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
            redirectAttributes.addFlashAttribute("successMessage", "Đã nhận lại sách & hoàn tiền cho đơn #" + id + ". Tồn kho sách đã được cập nhật lại.");
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
