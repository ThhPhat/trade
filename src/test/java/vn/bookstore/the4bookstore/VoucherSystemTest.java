package vn.bookstore.the4bookstore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.controller.CartController;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.repository.KhuyenMaiRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VoucherSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

    @Autowired
    private CartController cartController;

    @Test
    @DisplayName("Kiểm tra danh sách voucher hiện có trong DB và xóa các mã mẫu cũ nếu có")
    @Transactional
    void checkAndCleanOldSampleVouchers() {
        List<KhuyenMai> allKM = khuyenMaiRepository.findAll();
        System.out.println("=== TỔNG SỐ KHUYẾN MÃI TRONG DATABASE: " + allKM.size() + " ===");
        for (KhuyenMai km : allKM) {
            System.out.println("ID=" + km.getMaKM() + ", Code=" + km.getMaCode() + ", Ten=" + km.getTenKM() + ", Status=" + km.getTrangThai());
        }

        // Đảm bảo các mã mặc định cũ không tồn tại
        Optional<KhuyenMai> old1 = khuyenMaiRepository.findByMaCode("THE4BOOK15");
        old1.ifPresent(km -> khuyenMaiRepository.delete(km));

        Optional<KhuyenMai> old2 = khuyenMaiRepository.findByMaCode("FREESHIP");
        old2.ifPresent(km -> khuyenMaiRepository.delete(km));

        Optional<KhuyenMai> old3 = khuyenMaiRepository.findByMaCode("BOOK15");
        old3.ifPresent(km -> khuyenMaiRepository.delete(km));

        Optional<KhuyenMai> old4 = khuyenMaiRepository.findByMaCode("SALE15");
        old4.ifPresent(km -> khuyenMaiRepository.delete(km));
    }

    @Test
    @DisplayName("Mã không tồn tại hoặc mã có sẵn cũ (THE4BOOK15) phải bị từ chối")
    void testOldDefaultVouchersAreRejected() {
        Map<String, Object> res1 = cartController.apiValidateVoucher("THE4BOOK15", 500000);
        assertFalse((Boolean) res1.get("valid"), "THE4BOOK15 không được phép hợp lệ nếu không có trong DB");

        Map<String, Object> res2 = cartController.apiValidateVoucher("BOOK15", 500000);
        assertFalse((Boolean) res2.get("valid"), "BOOK15 không được phép hợp lệ nếu không có trong DB");

        Map<String, Object> res3 = cartController.apiValidateVoucher("UNKNOWN_CODE_999", 500000);
        assertFalse((Boolean) res3.get("valid"), "Mã tùy ý không có trong DB phải bị từ chối");
    }

    @Test
    @DisplayName("Chỉ mã được tạo trong Admin (lưu vào DB) mới được chấp nhận khi hợp lệ")
    @Transactional
    void testAdminCreatedVoucherIsValidatedSuccessfully() {
        String testCode = "ADMIN_TEST_" + System.currentTimeMillis();

        KhuyenMai km = new KhuyenMai();
        km.setMaCode(testCode);
        km.setTenKM("Voucher thử nghiệm từ Admin");
        km.setLoaiGiam("PhanTram");
        km.setGiaTriGiam(20);
        km.setGiamToiDa(50000);
        km.setDonToiThieu(100000);
        km.setSoLuongToiDa(10);
        km.setSoLuongDaDung(0);
        km.setNgayBatDau(LocalDateTime.now().minusDays(1));
        km.setNgayKetThuc(LocalDateTime.now().plusDays(10));
        km.setTrangThai("HoatDong");
        km.setHienThiCongKhai(true);
        km = khuyenMaiRepository.save(km);

        try {
            // Test đơn chưa đủ giá trị tối thiểu
            Map<String, Object> resBelowMin = cartController.apiValidateVoucher(testCode, 50000);
            assertFalse((Boolean) resBelowMin.get("valid"));
            assertTrue(resBelowMin.get("message").toString().contains("tối thiểu"));

            // Test đơn đủ điều kiện
            Map<String, Object> resValid = cartController.apiValidateVoucher(testCode, 200000);
            assertTrue((Boolean) resValid.get("valid"));
            assertEquals(40000, ((Number) resValid.get("discountAmount")).intValue()); // 20% của 200k = 40k

            // Test trần giảm tối đa (500k * 20% = 100k > max 50k)
            Map<String, Object> resCap = cartController.apiValidateVoucher(testCode, 500000);
            assertTrue((Boolean) resCap.get("valid"));
            assertEquals(50000, ((Number) resCap.get("discountAmount")).intValue());
        } finally {
            khuyenMaiRepository.delete(km);
        }
    }

    @Autowired
    private vn.bookstore.the4bookstore.repository.TaiKhoanRepository taiKhoanRepository;

    @Test
    @DisplayName("Truy cập trang /profile thành công mà không có lỗi Thymeleaf")
    @Transactional
    void testProfilePageRendersWithoutErrors() throws Exception {
        vn.bookstore.the4bookstore.entity.TaiKhoan tk = new vn.bookstore.the4bookstore.entity.TaiKhoan();
        tk.setTenDangNhap("testuser_profile_" + System.currentTimeMillis());
        tk.setEmail("testuser_profile_" + System.currentTimeMillis() + "@example.com");
        tk.setMatKhauHash("123456");
        tk.setVaiTro("KHACHHANG");
        tk.setTrangThai("HoatDong");
        tk = taiKhoanRepository.save(tk);

        vn.bookstore.the4bookstore.security.CustomUserDetails userDetails = new vn.bookstore.the4bookstore.security.CustomUserDetails(tk);
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        mockMvc.perform(get("/profile").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/index"))
                .andExpect(model().attributeExists("availableVouchers", "totalVouchers"));
    }

    @Autowired
    private vn.bookstore.the4bookstore.controller.VoucherController voucherController;

    @Autowired
    private vn.bookstore.the4bookstore.repository.KhachHangRepository khachHangRepository;

    @Autowired
    private vn.bookstore.the4bookstore.repository.VoucherDaLuuRepository voucherDaLuuRepository;

    @Test
    @DisplayName("Trang chủ render thành công với model publicVouchers và savedVoucherIds")
    void testHomePageVoucherSection() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attributeExists("publicVouchers", "savedVoucherIds"));
    }

    @Test
    @DisplayName("Trang Săn Voucher /san-voucher render thành công")
    void testSanVoucherPage() throws Exception {
        mockMvc.perform(get("/san-voucher"))
                .andExpect(status().isOk())
                .andExpect(view().name("voucher/san-voucher"))
                .andExpect(model().attributeExists("publicVouchers", "savedIds"));
    }

    @Test
    @DisplayName("Quy trình Săn Voucher: Ví ban đầu trống -> Lưu mã -> Mã vào ví -> Kiểm tra điều kiện đơn hàng")
    @Transactional
    void testVoucherHuntAndSmartWalletFlow() {
        // 1. Tạo tài khoản & khách hàng mới
        vn.bookstore.the4bookstore.entity.TaiKhoan tk = new vn.bookstore.the4bookstore.entity.TaiKhoan();
        tk.setTenDangNhap("hunt_user_" + System.currentTimeMillis());
        tk.setEmail("hunt_user_" + System.currentTimeMillis() + "@gmail.com");
        tk.setMatKhauHash("123456");
        tk.setVaiTro("KHACHHANG");
        tk.setTrangThai("HoatDong");
        tk = taiKhoanRepository.save(tk);

        vn.bookstore.the4bookstore.entity.KhachHang kh = new vn.bookstore.the4bookstore.entity.KhachHang();
        kh.setHoTen("Nguyễn Văn Săn Voucher");
        kh.setEmail(tk.getEmail());
        kh.setTaiKhoan(tk);
        kh = khachHangRepository.save(kh);

        vn.bookstore.the4bookstore.security.CustomUserDetails userDetails = new vn.bookstore.the4bookstore.security.CustomUserDetails(tk);
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 2. Tạo 1 mã voucher công khai yêu cầu đơn tối thiểu 500k
        String code = "SAN50K_" + System.currentTimeMillis();
        KhuyenMai km = new KhuyenMai();
        km.setMaCode(code);
        km.setTenKM("Giảm 50K cho đơn 500K");
        km.setLoaiGiam("TienCoDinh");
        km.setGiaTriGiam(50000);
        km.setDonToiThieu(500000);
        km.setSoLuongToiDa(100);
        km.setSoLuongDaDung(0);
        km.setNgayBatDau(LocalDateTime.now().minusDays(1));
        km.setNgayKetThuc(LocalDateTime.now().plusDays(7));
        km.setTrangThai("HoatDong");
        km.setHienThiCongKhai(true);
        km = khuyenMaiRepository.save(km);

        try {
            // Bước A: Ban đầu ví người dùng phải TRỐNG
            Map<String, Object> walletBefore = voucherController.getMyWallet(200000, auth);
            assertTrue((Boolean) walletBefore.get("success"));
            List<?> listBefore = (List<?>) walletBefore.get("vouchers");
            assertTrue(listBefore.isEmpty(), "Ban đầu ví người dùng phải trống");

            // Bước B: Bấm "Lưu mã"
            Map<String, Object> saveResult = voucherController.saveVoucher(km.getMaKM(), auth);
            assertTrue((Boolean) saveResult.get("success"), "Lưu mã phải thành công");

            // Lưu lại lần 2 phải báo đã lưu
            Map<String, Object> saveAgain = voucherController.saveVoucher(km.getMaKM(), auth);
            assertFalse((Boolean) saveAgain.get("success"));
            assertTrue((Boolean) saveAgain.get("alreadySaved"));

            // Bước C: Mở ví khi đơn hàng mới 200k (< 500k) -> Mã phải bị mờ (eligible = false) và ghi rõ cần mua thêm 300k
            Map<String, Object> walletAt200k = voucherController.getMyWallet(200000, auth);
            List<Map<String, Object>> vList200k = (List<Map<String, Object>>) walletAt200k.get("vouchers");
            assertEquals(1, vList200k.size());
            Map<String, Object> voucherItem = vList200k.get(0);
            assertFalse((Boolean) voucherItem.get("eligible"), "Đơn 200k chưa đủ 500k nên eligible phải false (mờ)");
            assertNotNull(voucherItem.get("reason"));
            assertTrue(voucherItem.get("reason").toString().contains("Mua thêm 300,000"), "Phải thông báo mua thêm 300,000 để sử dụng");

            // Bước D: Mở ví khi đơn hàng 600k (>= 500k) -> Mã phải sáng (eligible = true)
            Map<String, Object> walletAt600k = voucherController.getMyWallet(600000, auth);
            List<Map<String, Object>> vList600k = (List<Map<String, Object>>) walletAt600k.get("vouchers");
            Map<String, Object> voucherItem600k = vList600k.get(0);
            assertTrue((Boolean) voucherItem600k.get("eligible"), "Đơn 600k đã đủ 500k nên eligible phải true (sáng)");
            assertEquals(50000, ((Number) voucherItem600k.get("discountPreview")).intValue());
        } finally {
            voucherDaLuuRepository.deleteAll(voucherDaLuuRepository.findByKhachHangOrderByNgayLuuDesc(kh));
            khuyenMaiRepository.delete(km);
            khachHangRepository.delete(kh);
            taiKhoanRepository.delete(tk);
        }
    }
}
