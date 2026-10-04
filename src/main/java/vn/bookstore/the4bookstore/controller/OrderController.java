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

    public OrderController(DonHangService donHangService,
                           KhachHangRepository khachHangRepository,
                           TaiKhoanRepository taiKhoanRepository,
                           DonHangRepository donHangRepository,
                           ThanhToanRepository thanhToanRepository) {
        this.donHangService = donHangService;
        this.khachHangRepository = khachHangRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.donHangRepository = donHangRepository;
        this.thanhToanRepository = thanhToanRepository;
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
    public String orderHistory(Authentication authentication, Model model) {
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            return "redirect:/login";
        }

        List<DonHang> donHangs = donHangService.getOrdersByKhachHang(kh);
        long totalOrders = donHangRepository.countByKhachHang(kh);
        Long completedOrders = donHangRepository.countByKhachHangAndTrangThai(kh, "DaGiao");

        model.addAttribute("donHangs", donHangs);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders != null ? completedOrders : 0L);
        model.addAttribute("khachHang", kh);

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
