package vn.bookstore.the4bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import vn.bookstore.the4bookstore.service.DonHangService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/don-hang")
public class OrderController {

    private final DonHangService donHangService;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DonHangRepository donHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final vn.bookstore.the4bookstore.service.VNPayService vnPayService;

    public OrderController(DonHangService donHangService,
                           KhachHangRepository khachHangRepository,
                           TaiKhoanRepository taiKhoanRepository,
                           DonHangRepository donHangRepository,
                           ThanhToanRepository thanhToanRepository,
                           vn.bookstore.the4bookstore.service.VNPayService vnPayService) {
        this.donHangService = donHangService;
        this.khachHangRepository = khachHangRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.donHangRepository = donHangRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.vnPayService = vnPayService;
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
                if ("ChoXuLy".equalsIgnoreCase(trangThai)) return "ChoXuLy".equalsIgnoreCase(st) || "DonHangMoi".equalsIgnoreCase(st);
                if ("DaXacNhan".equalsIgnoreCase(trangThai)) return "DaXacNhan".equalsIgnoreCase(st);
                if ("DangGiao".equalsIgnoreCase(trangThai)) return "DangGiao".equalsIgnoreCase(st) || "DaLayHang".equalsIgnoreCase(st);
                if ("DaGiao".equalsIgnoreCase(trangThai)) return "DaGiao".equalsIgnoreCase(st);
                if ("DaHuy".equalsIgnoreCase(trangThai)) return "DaHuy".equalsIgnoreCase(st) || "Huy".equalsIgnoreCase(st);
                if ("TraHangHoanTien".equalsIgnoreCase(trangThai)) return "TraHangHoanTien".equalsIgnoreCase(st) || "TranhChap".equalsIgnoreCase(st);
                return st.equalsIgnoreCase(trangThai);
            }).toList();
        }

        long totalOrders = allOrders.size();
        Long completedOrders = allOrders.stream().filter(dh -> "DaGiao".equalsIgnoreCase(dh.getTrangThai())).count();

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

            // Kiểm tra quyền sở hữu
            if (!donHang.getKhachHang().getMaKH().equals(kh.getMaKH())) {
                return "redirect:/don-hang";
            }

            model.addAttribute("donHang", donHang);
            model.addAttribute("khachHang", kh);
            model.addAttribute("thanhToan", thanhToanRepository.findFirstByDonHangOrderByMaThanhToanDesc(donHang).orElse(null));

            return "order/detail";
        } catch (Exception e) {
            return "redirect:/don-hang";
        }
    }

    // ==================== Hủy đơn hàng ====================

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
            donHangService.cancelOrder(id, kh, lyDoHuy);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #TB-" + id + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

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

        if (!"DaGiao".equalsIgnoreCase(dh.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể yêu cầu Trả hàng - Hoàn tiền với đơn hàng đã giao thành công!");
            return "redirect:/don-hang/" + id;
        }

        dh.setTrangThai("TraHangHoanTien");
        dh.setLyDoTraHang(lyDoTraHang != null ? lyDoTraHang.trim() : "Khách hàng yêu cầu đổi trả");
        donHangRepository.save(dh);
        redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu Trả hàng - Hoàn tiền đã gửi đến Shop thành công!");
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

    // ==================== Callback VNPay Return ====================
    @GetMapping("/vnpay-return")
    public String vnpayReturn(jakarta.servlet.http.HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> fields = new HashMap<>();
        for (java.util.Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }

        String orderIdStr = fields.get("vnp_TxnRef");
        String responseCode = fields.get("vnp_ResponseCode");
        boolean checkSignature = vnPayService.verifyCallback(fields);

        if (orderIdStr != null) {
            try {
                int maDH = Integer.parseInt(orderIdStr);
                DonHang dh = donHangService.getOrderById(maDH);

                if (checkSignature && "00".equals(responseCode)) {
                    dh.setTrangThaiThanhToan("DaThanhToan");
                    ThanhToan tt = thanhToanRepository.findFirstByDonHangOrderByMaThanhToanDesc(dh).orElse(null);
                    if (tt != null) {
                        tt.setTrangThai("ThanhCong");
                        thanhToanRepository.save(tt);
                    }
                    donHangRepository.save(dh);
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toán trực tuyến VNPay thành công cho đơn hàng #TB-" + maDH + "!");
                } else {
                    dh.setTrangThaiThanhToan("ThatBai");
                    donHangRepository.save(dh);
                    redirectAttributes.addFlashAttribute("errorMessage", "Giao dịch VNPay không thành công hoặc đã bị hủy (Mã phản hồi: " + responseCode + ")!");
                }
                return "redirect:/don-hang/" + maDH;
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý kết quả VNPay: " + e.getMessage());
            }
        }
        return "redirect:/don-hang";
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
