package vn.bookstore.the4bookstore.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import vn.bookstore.the4bookstore.service.DonHangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/don-hang")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Value("${vietqr.webhook-secret:}")
    private String webhookSecret;

    private final DonHangService donHangService;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DonHangRepository donHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final vn.bookstore.the4bookstore.service.VietQRService vietQRService;
    private final ShopRepository shopRepository;

    public OrderController(DonHangService donHangService,
                           KhachHangRepository khachHangRepository,
                           TaiKhoanRepository taiKhoanRepository,
                           DonHangRepository donHangRepository,
                           ThanhToanRepository thanhToanRepository,
                           vn.bookstore.the4bookstore.service.VietQRService vietQRService,
                           ShopRepository shopRepository) {
        this.donHangService = donHangService;
        this.khachHangRepository = khachHangRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.donHangRepository = donHangRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.vietQRService = vietQRService;
        this.shopRepository = shopRepository;
    }

    // ==================== Helper: Lấy TaiKhoan từ Authentication ====================

    private TaiKhoan getCurrentTaiKhoan(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            if (userDetails.getTaiKhoan() != null && userDetails.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(userDetails.getTaiKhoan().getMaTaiKhoan())
                        .orElse(userDetails.getTaiKhoan());
            }
        }
        if (principal instanceof CustomOAuth2User oAuth2User) {
            if (oAuth2User.getTaiKhoan() != null && oAuth2User.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(oAuth2User.getTaiKhoan().getMaTaiKhoan()).orElse(null);
            }
            if (oAuth2User.getEmail() != null && !oAuth2User.getEmail().isBlank()) {
                return taiKhoanRepository.findByEmail(oAuth2User.getEmail()).orElse(null);
            }
        }
        if (principal instanceof CustomOidcUser oidcUser) {
            if (oidcUser.getTaiKhoan() != null && oidcUser.getTaiKhoan().getMaTaiKhoan() != null) {
                return taiKhoanRepository.findById(oidcUser.getTaiKhoan().getMaTaiKhoan()).orElse(null);
            }
            if (oidcUser.getEmail() != null && !oidcUser.getEmail().isBlank()) {
                return taiKhoanRepository.findByEmail(oidcUser.getEmail()).orElse(null);
            }
        }
        if (principal instanceof OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isBlank()) {
                return taiKhoanRepository.findByEmail(email).orElse(null);
            }
        }
        if (principal instanceof OAuth2User oauth2User) {
            Object emailObj = oauth2User.getAttribute("email");
            if (emailObj != null && !emailObj.toString().isBlank()) {
                return taiKhoanRepository.findByEmail(emailObj.toString()).orElse(null);
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

    private KhachHang getCurrentKhachHang(Authentication authentication) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) return null;

        return khachHangRepository.findByTaiKhoan(tk).orElseGet(() -> {
            KhachHang kh = new KhachHang();
            kh.setTaiKhoan(tk);
            kh.setHoTen(tk.getTenDangNhap());
            kh.setEmail(tk.getEmail());
            kh.setNgayDangKy(LocalDateTime.now());
            return khachHangRepository.save(kh);
        });
    }

    private Shop getCurrentShop(Authentication authentication) {
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) return null;
        Optional<Shop> userShop = shopRepository.findByTaiKhoan(tk);
        if (userShop.isPresent()) {
            return userShop.get();
        }
        if ("ADMIN".equalsIgnoreCase(tk.getVaiTro()) || "MANAGER".equalsIgnoreCase(tk.getVaiTro()) || "QUANLY".equalsIgnoreCase(tk.getVaiTro())) {
            return shopRepository.findById(1).orElse(null);
        }
        return null;
    }

    // ==================== Lịch sử đơn hàng ====================

    @GetMapping
    public String orderHistory(@RequestParam(value = "trangThai", required = false) String trangThai,
                               Authentication authentication, Model model) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            return "redirect:/login";
        }

        List<DonHang> allOrders = donHangService.getOrdersByKhachHang(kh);
        List<DonHang> donHangs = allOrders;

        if (trangThai != null && !trangThai.isBlank() && !"ALL".equalsIgnoreCase(trangThai)) {
            donHangs = allOrders.stream().filter(dh -> {
                String st = dh.getTrangThai();
                if (st == null) return false;
                if ("ChoXuLy".equalsIgnoreCase(trangThai) || "DonHangMoi".equalsIgnoreCase(trangThai)) {
                    return "ChoXuLy".equalsIgnoreCase(st) || "DonHangMoi".equalsIgnoreCase(st);
                }
                if ("DaXacNhan".equalsIgnoreCase(trangThai)) {
                    return "DaXacNhan".equalsIgnoreCase(st) || "YeuCauHuy".equalsIgnoreCase(st);
                }
                if ("DangGiao".equalsIgnoreCase(trangThai)) {
                    return "DangGiao".equalsIgnoreCase(st) || "DaLayHang".equalsIgnoreCase(st);
                }
                if ("DaGiao".equalsIgnoreCase(trangThai)) {
                    return "DaGiao".equalsIgnoreCase(st);
                }
                if ("HoanTat".equalsIgnoreCase(trangThai)) {
                    return "HoanTat".equalsIgnoreCase(st);
                }
                if ("DaHuy".equalsIgnoreCase(trangThai)) {
                    return "DaHuy".equalsIgnoreCase(st) || "Huy".equalsIgnoreCase(st);
                }
                if ("TraHangHoanTien".equalsIgnoreCase(trangThai)) {
                    return "TraHangHoanTien".equalsIgnoreCase(st) || "TranhChap".equalsIgnoreCase(st);
                }
                return st.equalsIgnoreCase(trangThai);
            }).toList();
        }

        long totalOrders = allOrders.size();
        Long completedOrders = allOrders.stream().filter(dh -> "DaGiao".equalsIgnoreCase(dh.getTrangThai()) || "HoanTat".equalsIgnoreCase(dh.getTrangThai())).count();

        model.addAttribute("donHangs", donHangs);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("khachHang", kh);
        model.addAttribute("currentTab", (trangThai != null && !trangThai.isBlank()) ? trangThai : "ALL");

        return "order/history";
    }

    // ==================== Chi tiết đơn hàng ====================

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Integer id,
                              Authentication authentication,
                              Model model) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            return "redirect:/login";
        }

        try {
            DonHang donHang = donHangService.getOrderById(id);

            // Kiểm tra quyền sở hữu (hoặc tài khoản quản trị viên / nhân viên)
            boolean isStaff = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_MANAGER")
                            || a.getAuthority().equals("ROLE_QUANLY"));

            if (!isStaff && (donHang.getKhachHang() == null || !donHang.getKhachHang().getMaKH().equals(kh.getMaKH()))) {
                return "redirect:/don-hang";
            }

            model.addAttribute("donHang", donHang);
            model.addAttribute("khachHang", kh);
            model.addAttribute("thanhToan", thanhToanRepository.findFirstByDonHangOrderByMaThanhToanDesc(donHang).orElse(null));

            if (("ChuyenKhoan".equalsIgnoreCase(donHang.getPhuongThucThanhToan()) || "VIETQR".equalsIgnoreCase(donHang.getPhuongThucThanhToan()))
                    && !"DaThanhToan".equalsIgnoreCase(donHang.getTrangThaiThanhToan())
                    && !"DaHuy".equalsIgnoreCase(donHang.getTrangThai())
                    && !"Huy".equalsIgnoreCase(donHang.getTrangThai())) {
                int amount = donHang.getTongTien() != null ? donHang.getTongTien() : 0;
                model.addAttribute("vietQrUrl", vietQRService.generateQrUrl(donHang.getMaDH(), amount));
                model.addAttribute("bankId", vietQRService.getBankId());
                model.addAttribute("accountNo", vietQRService.getAccountNo());
                model.addAttribute("accountName", vietQRService.getAccountName());
            }

            return "order/detail";
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(OrderController.class).error("Lỗi khi xem chi tiết đơn hàng #{}: {}", id, e.getMessage());
            return "redirect:/don-hang";
        }
    }

    // ==================== Hủy đơn hàng & Yêu cầu hủy ====================

    @PostMapping("/{id}/huy")
    public String cancelOrder(@PathVariable("id") Integer id,
                              @RequestParam(value = "lyDoHuy", required = false) String lyDoHuy,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            return "redirect:/login";
        }

        try {
            DonHang dh = donHangService.getOrderById(id);
            if (!dh.getKhachHang().getMaKH().equals(kh.getMaKH())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền can thiệp vào đơn hàng này!");
                return "redirect:/don-hang";
            }

            if ("DaGiao".equalsIgnoreCase(dh.getTrangThai()) || "HoanTat".equalsIgnoreCase(dh.getTrangThai())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã giao thành công không thể hủy. Vui lòng chọn 'Yêu cầu Trả hàng - Hoàn tiền' nếu bạn muốn hoàn hàng!");
                return "redirect:/don-hang/" + id;
            }

            if ("DaXacNhan".equalsIgnoreCase(dh.getTrangThai())) {
                // Đơn đã xác nhận: Không thể tự hủy trực tiếp -> Chuyển thành Yêu Cầu Hủy gửi shop
                dh.setTrangThai("YeuCauHuy");
                dh.setLyDoHuy(lyDoHuy != null && !lyDoHuy.isBlank() ? lyDoHuy.trim() : "Khách hàng gửi yêu cầu hủy đơn");
                donHangRepository.save(dh);
                redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận. Yêu cầu hủy đã được chuyển đến Shop để xem xét và xử lý!");
            } else if ("ChoXuLy".equalsIgnoreCase(dh.getTrangThai()) || "DonHangMoi".equalsIgnoreCase(dh.getTrangThai())) {
                // Đơn mới: Tự động hủy và nhả lại tồn kho
                donHangService.cancelOrder(id, kh, lyDoHuy);
                redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #TB-" + id + " thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng ở trạng thái hiện tại (" + dh.getTrangThai() + ")!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/don-hang/" + id;
    }

    // ==================== Khách xác nhận "Đã nhận được hàng" (Hoàn Tất) ====================
    @PostMapping("/{id}/da-nhan-hang")
    public String confirmReceived(@PathVariable("id") Integer id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) return "redirect:/login";

        DonHang dh = donHangService.getOrderById(id);
        if (!dh.getKhachHang().getMaKH().equals(kh.getMaKH())) return "redirect:/don-hang";

        if (!"DaGiao".equalsIgnoreCase(dh.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng ở trạng thái 'Đã giao'!");
            return "redirect:/don-hang/" + id;
        }

        dh.setTrangThai("HoanTat");
        dh.setNgayHoanThanh(LocalDateTime.now());
        dh.setTrangThaiThanhToan("DaThanhToan");

        // Quyết toán tiền về ví Shop = Tổng tiền - Phí sàn (% Commission)
        java.math.BigDecimal feeRate = (dh.getShop() != null && dh.getShop().getChietKhauPhanTram() != null)
                ? dh.getShop().getChietKhauPhanTram()
                : new java.math.BigDecimal("5.00");
        int tongTien = dh.getTongTien() != null ? dh.getTongTien() : 0;
        int feeAmount = feeRate.multiply(new java.math.BigDecimal(tongTien)).divide(new java.math.BigDecimal(100), java.math.RoundingMode.HALF_UP).intValue();

        dh.setChietKhauAppPhanTram(feeRate);
        dh.setTienPhiSan(feeAmount);
        dh.setTienThucNhanShop(Math.max(0, tongTien - feeAmount));

        donHangRepository.save(dh);
        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã xác nhận nhận hàng! Đơn hàng đã hoàn tất thành công. Bạn có thể để lại đánh giá cho sản phẩm ngay bây giờ.");
        return "redirect:/don-hang/" + id;
    }

    // ==================== Yêu cầu Trả hàng - Hoàn tiền ====================
    @PostMapping("/{id}/yeu-cau-tra-hang")
    public String returnOrder(@PathVariable("id") Integer id,
                              @RequestParam("lyDoTraHang") String lyDoTraHang,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) return "redirect:/login";

        DonHang dh = donHangService.getOrderById(id);
        if (!dh.getKhachHang().getMaKH().equals(kh.getMaKH())) return "redirect:/don-hang";

        if ("ChoXuLy".equalsIgnoreCase(dh.getTrangThai()) || "DonHangMoi".equalsIgnoreCase(dh.getTrangThai()) || "ChoDuyet".equalsIgnoreCase(dh.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng chưa được xác nhận, vui lòng sử dụng chức năng Hủy đơn thay vì Yêu cầu Hoàn hàng!");
            return "redirect:/don-hang/" + id;
        }

        if ("DaHuy".equalsIgnoreCase(dh.getTrangThai()) || "Huy".equalsIgnoreCase(dh.getTrangThai()) || "DaHoan".equalsIgnoreCase(dh.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã kết thúc không thể yêu cầu hoàn hàng!");
            return "redirect:/don-hang/" + id;
        }

        dh.setTrangThai("TraHangHoanTien");
        dh.setLyDoTraHang(lyDoTraHang != null ? lyDoTraHang.trim() : "Khách hàng yêu cầu đổi trả");
        donHangRepository.save(dh);
        redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu Trả hàng - Hoàn tiền đã gửi đến hệ thống xử lý thành công!");
        return "redirect:/don-hang/" + id;
    }

    // ==================== Khiếu nại lên Quản lý sàn (Tranh Chấp) ====================
    @PostMapping("/{id}/khieu-nai")
    public String disputeOrder(@PathVariable("id") Integer id,
                               @RequestParam(value = "ghiChuTranhChap", required = false) String ghiChuTranhChap,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) return "redirect:/login";

        DonHang dh = donHangService.getOrderById(id);
        if (!dh.getKhachHang().getMaKH().equals(kh.getMaKH())) return "redirect:/don-hang";

        dh.setTrangThai("TranhChap");
        if (ghiChuTranhChap != null && !ghiChuTranhChap.isBlank()) {
            dh.setLyDoTraHang((dh.getLyDoTraHang() != null ? dh.getLyDoTraHang() + " | Khiếu nại khách: " : "") + ghiChuTranhChap);
        }
        donHangRepository.save(dh);
        redirectAttributes.addFlashAttribute("successMessage", "Đã khiếu nại lên Ban Quản Lý (Manager) sàn để giải quyết phân xử!");
        return "redirect:/don-hang/" + id;
    }

    // ==================== Khách báo "Tôi đã chuyển khoản" ====================
    @PostMapping("/{id}/bao-da-chuyen-tien")
    public String reportPaid(@PathVariable("id") Integer id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) return "redirect:/login";

        DonHang dh = donHangService.getOrderById(id);
        if (!dh.getKhachHang().getMaKH().equals(kh.getMaKH())) return "redirect:/don-hang";

        if ("ChoThanhToan".equalsIgnoreCase(dh.getTrangThai()) || "ChoXuLy".equalsIgnoreCase(dh.getTrangThai())) {
            dh.setTrangThaiThanhToan("ChoDuyetThanhToan");
            donHangRepository.save(dh);
            redirectAttributes.addFlashAttribute("successMessage", "Hệ thống đã ghi nhận thông báo chuyển tiền của bạn. Nhân viên / Shop sẽ đối soát và xử lý đơn ngay!");
        }
        return "redirect:/don-hang/" + id;
    }

    // ==================== Quản lý/Admin/Shop xác nhận đã nhận tiền ====================
    @PostMapping("/{id}/xac-nhan-thanh-toan")
    public String confirmPaymentByShop(@PathVariable("id") Integer id,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        boolean isStaff = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_QUANLY")
                        || a.getAuthority().equals("ROLE_VENDOR")
                        || a.getAuthority().equals("ROLE_GIAN_HANG"));

        if (!isStaff) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này!");
            return "redirect:/don-hang/" + id;
        }

        DonHang dh = donHangService.getOrderById(id);

        boolean isAdminOrManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_QUANLY"));

        if (!isAdminOrManager) {
            Shop currentShop = getCurrentShop(authentication);
            if (currentShop == null || dh.getShop() == null || !dh.getShop().getMaShop().equals(currentShop.getMaShop())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xác nhận thanh toán cho đơn hàng của shop khác!");
            }
        }

        dh.setTrangThaiThanhToan("DaThanhToan");
        if ("ChoThanhToan".equalsIgnoreCase(dh.getTrangThai())) {
            dh.setTrangThai("ChoXuLy");
        }
        ThanhToan tt = thanhToanRepository.findFirstByDonHangOrderByMaThanhToanDesc(dh).orElse(null);
        if (tt != null) {
            tt.setTrangThai("ThanhCong");
            thanhToanRepository.save(tt);
        }
        donHangRepository.save(dh);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán thành công cho đơn hàng #TB-" + id + "!");
        return "redirect:/don-hang/" + id;
    }

    // ==================== API Kiểm tra trạng thái thanh toán Realtime (Polling) ====================
    @GetMapping("/api/{id}/trang-thai-thanh-toan")
    @ResponseBody
    public ResponseEntity<?> checkPaymentStatus(@PathVariable("id") Integer id) {
        try {
            DonHang dh = donHangService.getOrderById(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("maDH", dh.getMaDH());
            resp.put("trangThai", dh.getTrangThai());
            resp.put("trangThaiThanhToan", dh.getTrangThaiThanhToan());
            resp.put("paid", "DaThanhToan".equalsIgnoreCase(dh.getTrangThaiThanhToan()));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private boolean verifyWebhookSignature(HttpServletRequest request, String body) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            logger.warn("VietQR webhook secret not configured - accepting without verification");
            return true;
        }
        String signature = request.getHeader("X-Webhook-Signature");
        if (signature == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(computed.getBytes(), signature.getBytes());
        } catch (Exception e) {
            logger.error("Webhook signature verification failed", e);
            return false;
        }
    }

    // ==================== Webhook Ngân hàng / Casso / SePAY ====================
    @PostMapping({"/api/vietqr-webhook", "/api/webhooks/vietqr"})
    @ResponseBody
    public ResponseEntity<?> vietQrWebhook(HttpServletRequest request, @RequestBody(required = false) String rawBody) {
        try {
            if (rawBody == null || rawBody.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Empty payload"));
            }

            if (!verifyWebhookSignature(request, rawBody)) {
                logger.warn("VietQR webhook signature verification failed from IP: {}", request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid signature"));
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> payload = mapper.readValue(rawBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            logger.info("Received VietQR webhook from IP {}: {}", request.getRemoteAddr(), payload);

            // Hỗ trợ trích xuất nội dung từ Casso / SePAY / Webhook ngân hàng
            String content = "";
            int transferAmount = 0;

            if (payload.containsKey("content")) content = payload.get("content").toString();
            else if (payload.containsKey("description")) content = payload.get("description").toString();
            else if (payload.containsKey("addInfo")) content = payload.get("addInfo").toString();

            if (payload.containsKey("transferAmount")) transferAmount = ((Number) payload.get("transferAmount")).intValue();
            else if (payload.containsKey("amount")) transferAmount = ((Number) payload.get("amount")).intValue();

            // Tìm mã đơn hàng từ nội dung chuyển khoản (Ví dụ: "DH102" hoặc "DH 102")
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)DH\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                int orderId = Integer.parseInt(matcher.group(1));
                DonHang dh = donHangRepository.findById(orderId).orElse(null);

                if (dh != null) {
                    dh.setTrangThaiThanhToan("DaThanhToan");
                    if ("ChoThanhToan".equalsIgnoreCase(dh.getTrangThai())) {
                        dh.setTrangThai("ChoXuLy");
                    }
                    ThanhToan tt = thanhToanRepository.findFirstByDonHangOrderByMaThanhToanDesc(dh).orElse(null);
                    if (tt != null) {
                        tt.setTrangThai("ThanhCong");
                        thanhToanRepository.save(tt);
                    }
                    donHangRepository.save(dh);
                    return ResponseEntity.ok(Map.of("success", true, "message", "Đã khớp đơn hàng #TB-" + orderId + " và cập nhật Đã Thanh Toán!"));
                }
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy mã đơn hàng phù hợp trong nội dung: " + content));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== API Lấy thông báo của người dùng ====================
    @GetMapping("/api/thong-bao")
    @ResponseBody
    public ResponseEntity<?> getClientNotifications(Authentication authentication) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            return ResponseEntity.ok(List.of());
        }

        List<DonHang> orders = donHangRepository.findByKhachHangOrderByNgayDatDesc(kh);
        List<Map<String, Object>> notifs = orders.stream().limit(8).map(dh -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maDH", dh.getMaDH());
            map.put("tongTien", dh.getTongTien());
            map.put("trangThai", dh.getTrangThai());
            map.put("ngayDat", dh.getNgayDat() != null ? dh.getNgayDat().toString() : "");
            
            String statusDesc = switch (dh.getTrangThai()) {
                case "ChoXuLy" -> "Đơn hàng đang chờ xử lý";
                case "DaXacNhan" -> "Đã được người bán xác nhận";
                case "DangGiao" -> "Đang được vận chuyển đến bạn";
                case "DaGiao" -> "Đã giao hàng thành công";
                case "DaHuy", "Huy" -> "Đã bị hủy";
                default -> "Cập nhật trạng thái đơn";
            };
            map.put("moTa", statusDesc);
            return map;
        }).toList();

        return ResponseEntity.ok(notifs);
    }
}
