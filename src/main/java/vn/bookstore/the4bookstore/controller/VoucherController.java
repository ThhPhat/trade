package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.entity.VoucherDaLuu;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.KhuyenMaiRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.repository.VoucherDaLuuRepository;
import vn.bookstore.the4bookstore.security.CustomOAuth2User;
import vn.bookstore.the4bookstore.security.CustomOidcUser;
import vn.bookstore.the4bookstore.security.CustomUserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class VoucherController {

    private final KhuyenMaiRepository khuyenMaiRepository;
    private final VoucherDaLuuRepository voucherDaLuuRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    // ==================== HELPER: Lấy KhachHang từ Authentication ====================
    private KhachHang getCurrentKhachHang(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        TaiKhoan tk = getCurrentTaiKhoan(authentication);
        if (tk == null) return null;
        return khachHangRepository.findByTaiKhoan(tk).orElse(null);
    }

    private TaiKhoan getCurrentTaiKhoan(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails ud) {
            if (ud.getTaiKhoan() != null && ud.getTaiKhoan().getMaTaiKhoan() != null)
                return taiKhoanRepository.findById(ud.getTaiKhoan().getMaTaiKhoan()).orElse(ud.getTaiKhoan());
        }
        if (principal instanceof CustomOAuth2User o) {
            if (o.getTaiKhoan() != null && o.getTaiKhoan().getMaTaiKhoan() != null)
                return taiKhoanRepository.findById(o.getTaiKhoan().getMaTaiKhoan()).orElse(null);
            if (o.getEmail() != null && !o.getEmail().isBlank())
                return taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        }
        if (principal instanceof CustomOidcUser o) {
            if (o.getTaiKhoan() != null && o.getTaiKhoan().getMaTaiKhoan() != null)
                return taiKhoanRepository.findById(o.getTaiKhoan().getMaTaiKhoan()).orElse(null);
            if (o.getEmail() != null && !o.getEmail().isBlank())
                return taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        }
        if (principal instanceof OidcUser o) {
            if (o.getEmail() != null && !o.getEmail().isBlank())
                return taiKhoanRepository.findByEmail(o.getEmail()).orElse(null);
        }
        if (principal instanceof OAuth2User o) {
            Object em = o.getAttribute("email");
            if (em != null && !em.toString().isBlank())
                return taiKhoanRepository.findByEmail(em.toString()).orElse(null);
        }
        String name = authentication.getName();
        if (name != null && !name.isBlank())
            return taiKhoanRepository.findByEmail(name)
                    .or(() -> taiKhoanRepository.findByTenDangNhap(name)).orElse(null);
        return null;
    }

    // ==================== API: Lưu mã voucher vào ví cá nhân ====================
    @PostMapping("/api/voucher/luu")
    @ResponseBody
    public Map<String, Object> saveVoucher(@RequestParam("maKM") Integer maKM,
                                           Authentication authentication) {
        Map<String, Object> result = new HashMap<>();

        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập để lưu mã giảm giá!");
            result.put("requireLogin", true);
            return result;
        }

        Optional<KhuyenMai> kmOpt = khuyenMaiRepository.findById(maKM);
        if (kmOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Mã khuyến mãi không tồn tại!");
            return result;
        }

        KhuyenMai km = kmOpt.get();

        // Kiểm tra voucher còn hiệu lực
        if (!km.isDangDienRa()) {
            result.put("success", false);
            result.put("message", "Mã khuyến mãi đã hết hạn hoặc không khả dụng!");
            return result;
        }

        // Kiểm tra đã lưu chưa
        if (voucherDaLuuRepository.existsByKhachHangAndKhuyenMai(kh, km)) {
            result.put("success", false);
            result.put("message", "Bạn đã lưu mã này rồi!");
            result.put("alreadySaved", true);
            return result;
        }

        // Lưu voucher vào ví
        VoucherDaLuu saved = new VoucherDaLuu();
        saved.setKhachHang(kh);
        saved.setKhuyenMai(km);
        saved.setNgayLuu(LocalDateTime.now());
        saved.setTrangThai("ChuaDung");
        voucherDaLuuRepository.save(saved);

        result.put("success", true);
        result.put("message", "Đã lưu mã \"" + km.getTenKM() + "\" vào Ví Voucher của bạn!");
        return result;
    }

    // ==================== API: Lấy Ví Voucher (dùng cho cart modal) ====================
    @GetMapping("/api/voucher/vi")
    @ResponseBody
    public Map<String, Object> getMyWallet(@RequestParam(value = "subtotal", defaultValue = "0") Integer subtotal,
                                           Authentication authentication) {
        Map<String, Object> result = new HashMap<>();

        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh == null) {
            result.put("success", false);
            result.put("vouchers", Collections.emptyList());
            result.put("message", "Chưa đăng nhập");
            return result;
        }

        List<VoucherDaLuu> savedVouchers = voucherDaLuuRepository
                .findByKhachHangAndTrangThaiOrderByNgayLuuDesc(kh, "ChuaDung");

        List<Map<String, Object>> voucherList = new ArrayList<>();
        
        for (VoucherDaLuu v : savedVouchers) {
            KhuyenMai km = v.getKhuyenMai();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", v.getId());
            item.put("maKM", km.getMaKM());
            item.put("maCode", km.getMaCode());
            item.put("tenKM", km.getTenKM());
            item.put("loaiGiam", km.getLoaiGiam());
            item.put("giaTriGiam", km.getGiaTriGiam());
            item.put("giamToiDa", km.getGiamToiDa());
            item.put("donToiThieu", km.getDonToiThieu());
            item.put("moTa", km.getMoTa());

            // Kiểm tra voucher còn hiệu lực chung
            boolean expired = !km.isDangDienRa();
            item.put("expired", expired);

            // Kiểm tra đủ điều kiện đơn tối thiểu
            boolean eligible = !expired;
            String reason = null;
            if (!expired && km.getDonToiThieu() != null && km.getDonToiThieu() > 0 && subtotal < km.getDonToiThieu()) {
                eligible = false;
                int thieu = km.getDonToiThieu() - subtotal;
                reason = "Mua thêm " + String.format("%,d", thieu) + "đ để sử dụng";
            }
            item.put("eligible", eligible);
            item.put("reason", reason);

            // Tính discount dự kiến
            if (eligible) {
                int discount = 0;
                if ("PhanTram".equalsIgnoreCase(km.getLoaiGiam())) {
                    discount = (int) Math.round(subtotal * (km.getGiaTriGiam() / 100.0));
                    if (km.getGiamToiDa() != null && discount > km.getGiamToiDa()) discount = km.getGiamToiDa();
                } else if ("TienCoDinh".equalsIgnoreCase(km.getLoaiGiam())) {
                    discount = Math.min(subtotal, km.getGiaTriGiam());
                } else if ("Freeship".equalsIgnoreCase(km.getLoaiGiam())) {
                    discount = km.getGiaTriGiam() != null ? km.getGiaTriGiam() : 30000;
                }
                item.put("discountPreview", discount);
            }

            voucherList.add(item);
        }

        result.put("success", true);
        result.put("vouchers", voucherList);
        result.put("total", savedVouchers.size());
        return result;
    }

    // ==================== API: Lấy danh sách voucher công khai (cho homepage) ====================
    @GetMapping("/api/voucher/public")
    @ResponseBody
    public Map<String, Object> getPublicVouchers(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        
        List<KhuyenMai> publicVouchers = khuyenMaiRepository.findAll().stream()
                .filter(km -> "HoatDong".equalsIgnoreCase(km.getTrangThai()))
                .filter(km -> Boolean.TRUE.equals(km.getHienThiCongKhai()))
                .filter(km -> km.isDangDienRa())
                .toList();

        // Lấy danh sách ID đã lưu nếu user đã login
        Set<Integer> savedIds = new HashSet<>();
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh != null) {
            savedIds.addAll(voucherDaLuuRepository.findSavedVoucherIdsByKhachHang(kh));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (KhuyenMai km : publicVouchers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("maKM", km.getMaKM());
            item.put("tenKM", km.getTenKM());
            item.put("maCode", km.getMaCode());
            item.put("loaiGiam", km.getLoaiGiam());
            item.put("giaTriGiam", km.getGiaTriGiam());
            item.put("giamToiDa", km.getGiamToiDa());
            item.put("donToiThieu", km.getDonToiThieu());
            item.put("moTa", km.getMoTa());
            item.put("ngayKetThuc", km.getNgayKetThuc() != null ? km.getNgayKetThuc().toString() : null);
            item.put("saved", savedIds.contains(km.getMaKM()));
            list.add(item);
        }

        result.put("vouchers", list);
        result.put("loggedIn", kh != null);
        return result;
    }

    // ==================== Trang "Săn Khuyến Mãi" (full page) ====================
    @GetMapping("/san-voucher")
    public String sanVoucherPage(Authentication authentication, Model model) {
        
        List<KhuyenMai> publicVouchers = khuyenMaiRepository.findAll().stream()
                .filter(km -> "HoatDong".equalsIgnoreCase(km.getTrangThai()))
                .filter(km -> Boolean.TRUE.equals(km.getHienThiCongKhai()))
                .filter(km -> km.isDangDienRa())
                .toList();

        Set<Integer> savedIds = new HashSet<>();
        KhachHang kh = getCurrentKhachHang(authentication);
        if (kh != null) {
            savedIds.addAll(voucherDaLuuRepository.findSavedVoucherIdsByKhachHang(kh));
        }

        model.addAttribute("publicVouchers", publicVouchers);
        model.addAttribute("savedIds", savedIds);
        model.addAttribute("loggedIn", kh != null);
        return "voucher/san-voucher";
    }
}
