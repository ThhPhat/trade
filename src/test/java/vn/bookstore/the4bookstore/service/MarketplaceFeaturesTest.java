package vn.bookstore.the4bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceFeaturesTest {

    @Mock private SanPhamYeuThichRepository yeuThichRepository;
    @Mock private SanPhamDaXemRepository daXemRepository;
    @Mock private SanPhamRepository sanPhamRepository;
    @Mock private DanhGiaRepository danhGiaRepository;
    @Mock private DanhGiaMediaRepository danhGiaMediaRepository;
    @Mock private DonHangRepository donHangRepository;
    @Mock private ChiTietDonHangRepository chiTietDonHangRepository;

    @InjectMocks
    private UserInteractionService userInteractionService;

    private KhachHang khachHang;
    private SanPham sanPham;
    private DonHang donHang;

    @BeforeEach
    void setUp() {
        khachHang = new KhachHang();
        khachHang.setMaKH(10);
        khachHang.setHoTen("Nguyễn Văn A");

        sanPham = new SanPham();
        sanPham.setMaSP(100);
        sanPham.setTenSP("Đắc Nhân Tâm - Deluxe Edition");
        sanPham.setGiaBan(120000);
        sanPham.setTrangThai("HoatDong");

        donHang = new DonHang();
        donHang.setMaDH(500);
        donHang.setKhachHang(khachHang);
        donHang.setTrangThai("DaGiao");
        donHang.setTongTien(120000);
    }

    @Test
    @DisplayName("User: Toggle Favorite thêm vào wishlist khi chưa có")
    void testToggleFavorite_WhenNotFavorited_ShouldAdd() {
        when(sanPhamRepository.findById(100)).thenReturn(Optional.of(sanPham));
        when(yeuThichRepository.existsById(any(SanPhamYeuThichId.class))).thenReturn(false);

        boolean result = userInteractionService.toggleFavorite(khachHang, 100);

        assertTrue(result, "Lần đầu thích sản phẩm phải trả về true");
        verify(yeuThichRepository, times(1)).save(any(SanPhamYeuThich.class));
    }

    @Test
    @DisplayName("User: Toggle Favorite bỏ khỏi wishlist khi đã có")
    void testToggleFavorite_WhenAlreadyFavorited_ShouldRemove() {
        when(sanPhamRepository.findById(100)).thenReturn(Optional.of(sanPham));
        when(yeuThichRepository.existsById(any(SanPhamYeuThichId.class))).thenReturn(true);

        boolean result = userInteractionService.toggleFavorite(khachHang, 100);

        assertFalse(result, "Hủy thích sản phẩm phải trả về false");
        verify(yeuThichRepository, times(1)).deleteById(any(SanPhamYeuThichId.class));
    }

    @Test
    @DisplayName("User: Đánh giá dưới 50 ký tự phải ném ngoại lệ IllegalArgumentException")
    void testSubmitReview_TooShortContent_ShouldThrowException() {
        String shortContent = "Sách này rất hay, đóng gói đẹp!"; // 30 ký tự (< 50)

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.submitReview(khachHang, 100, 500, 5, shortContent, List.of());
        });

        assertTrue(ex.getMessage().contains("tối thiểu 50 ký tự"));
        verifyNoInteractions(danhGiaRepository);
    }

    @Test
    @DisplayName("User: Đơn hàng chưa giao thành công không được phép đánh giá")
    void testSubmitReview_OrderNotDelivered_ShouldThrowException() {
        donHang.setTrangThai("DangGiao");
        when(donHangRepository.findById(500)).thenReturn(Optional.of(donHang));

        String validContent = "Cuốn sách này cực kỳ truyền cảm hứng, chất lượng in ấn tuyệt vời và giao hàng rất nhanh chóng.";

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            userInteractionService.submitReview(khachHang, 100, 500, 5, validContent, List.of());
        });

        assertTrue(ex.getMessage().contains("giao thành công"));
    }

    @Test
    @DisplayName("User: Đánh giá hợp lệ với hình ảnh và video thành công")
    void testSubmitReview_ValidReviewWithMedia_Success() {
        when(donHangRepository.findById(500)).thenReturn(Optional.of(donHang));
        when(sanPhamRepository.findById(100)).thenReturn(Optional.of(sanPham));
        when(danhGiaRepository.save(any(DanhGia.class))).thenAnswer(i -> i.getArgument(0));

        String validContent = "Cuốn sách này cực kỳ truyền cảm hứng, chất lượng in ấn tuyệt vời và giao hàng rất nhanh chóng!";
        List<String> mediaUrls = List.of("/uploads/reviews/review1.jpg", "/uploads/reviews/unboxing.mp4");

        DanhGia result = userInteractionService.submitReview(khachHang, 100, 500, 5, validContent, mediaUrls);

        assertNotNull(result);
        assertEquals(5, result.getSoSao());
        assertEquals("HienThi", result.getTrangThai());
        verify(danhGiaRepository, times(1)).save(any(DanhGia.class));
        verify(danhGiaMediaRepository, times(2)).save(any(DanhGiaMedia.class));
    }

    @Test
    @DisplayName("Platform: Kiểm tra phân chia chiết khấu hoa hồng sàn và thực nhận của Shop")
    void testCommissionCalculation_ShouldSplitCorrectly() {
        double tongTien = 1000000.0;
        double tiLePhiSan = 5.0; // 5%

        double tienPhiSan = tongTien * (tiLePhiSan / 100.0);
        double tienThucNhanShop = tongTien - tienPhiSan;

        assertEquals(50000.0, tienPhiSan, 0.001, "Phí sàn 5% trên 1.000.000 phải là 50.000");
        assertEquals(950000.0, tienThucNhanShop, 0.001, "Shop thực nhận phải là 950.000");
    }
}
