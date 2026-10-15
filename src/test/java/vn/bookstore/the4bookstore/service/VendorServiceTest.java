package vn.bookstore.the4bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.entity.NhaXuatBan;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.repository.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock private ShopRepository shopRepository;
    @Mock private SanPhamRepository sanPhamRepository;
    @Mock private DonHangRepository donHangRepository;
    @Mock private KhuyenMaiRepository khuyenMaiRepository;
    @Mock private DanhMucRepository danhMucRepository;
    @Mock private NhaXuatBanRepository nhaXuatBanRepository;
    @Mock private TacGiaRepository tacGiaRepository;
    @Mock private SanPhamTacGiaRepository sanPhamTacGiaRepository;
    @Mock private SanPhamService sanPhamService;
    @Mock private ThanhToanRepository thanhToanRepository;

    @InjectMocks
    private VendorService vendorService;

    private Shop shop;
    private DanhMuc danhMuc;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setMaShop(1);
        shop.setTenShop("Gian Hàng Tri Thức");

        danhMuc = new DanhMuc();
        danhMuc.setMaDanhMuc(1);
        danhMuc.setTenDanhMuc("Văn học");

        when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
        when(danhMucRepository.findById(1)).thenReturn(Optional.of(danhMuc));
        when(sanPhamRepository.save(any(SanPham.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vendorService.setTacGiaRepository(tacGiaRepository);
        vendorService.setSanPhamTacGiaRepository(sanPhamTacGiaRepository);
    }

    @Test
    @DisplayName("Vendor tự nhập NXB mới: Hệ thống tự động tạo mới NhaXuatBan và gán vào sách")
    void testSaveShopProduct_WithNewPublisher_CreatesPublisher() {
        when(nhaXuatBanRepository.findByTenNXBIgnoreCase("NXB Tuổi Trẻ Mới")).thenReturn(Optional.empty());
        when(nhaXuatBanRepository.save(any(NhaXuatBan.class))).thenAnswer(invocation -> {
            NhaXuatBan nxb = invocation.getArgument(0);
            nxb.setMaNXB(99);
            return nxb;
        });

        SanPham sp = new SanPham();
        sp.setTenSP("Tuyển Tập Kỹ Năng Sống");
        sp.setGiaBan(120000);
        sp.setSoLuongTon(50);

        SanPham saved = vendorService.saveShopProduct(1, sp, 1, null, "NXB Tuổi Trẻ Mới");

        assertNotNull(saved);
        assertNotNull(saved.getNhaXuatBan());
        assertEquals("NXB Tuổi Trẻ Mới", saved.getNhaXuatBan().getTenNXB());
        assertEquals(99, saved.getNhaXuatBan().getMaNXB());
        verify(nhaXuatBanRepository, times(1)).save(any(NhaXuatBan.class));
    }

    @Test
    @DisplayName("Vendor nhập tên NXB đã có sẵn: Hệ thống tái sử dụng NhaXuatBan cũ, không tạo trùng")
    void testSaveShopProduct_WithExistingPublisher_ReusesExisting() {
        NhaXuatBan existingNxb = new NhaXuatBan();
        existingNxb.setMaNXB(5);
        existingNxb.setTenNXB("NXB Trẻ");

        when(nhaXuatBanRepository.findByTenNXBIgnoreCase("NXB Trẻ")).thenReturn(Optional.of(existingNxb));

        SanPham sp = new SanPham();
        sp.setTenSP("Đắc Nhân Tâm");
        sp.setGiaBan(85000);
        sp.setSoLuongTon(100);

        SanPham saved = vendorService.saveShopProduct(1, sp, 1, null, "  NXB Trẻ  ");

        assertNotNull(saved);
        assertNotNull(saved.getNhaXuatBan());
        assertEquals(5, saved.getNhaXuatBan().getMaNXB());
        assertEquals("NXB Trẻ", saved.getNhaXuatBan().getTenNXB());
        verify(nhaXuatBanRepository, never()).save(any(NhaXuatBan.class));
    }

    @Test
    @DisplayName("Vendor tự nhập Tác giả mới: Hệ thống tự động tạo TacGia và SanPhamTacGia liên kết với sách")
    void testSaveShopProduct_WithNewAuthor_CreatesTacGiaAndLinks() {
        when(tacGiaRepository.findByTenTacGiaIgnoreCase("Nguyễn Nhật Ánh")).thenReturn(Optional.empty());
        when(tacGiaRepository.save(any(vn.bookstore.the4bookstore.entity.TacGia.class))).thenAnswer(invocation -> {
            vn.bookstore.the4bookstore.entity.TacGia tg = invocation.getArgument(0);
            tg.setMaTacGia(10);
            return tg;
        });

        SanPham sp = new SanPham();
        sp.setTenSP("Mắt Biếc");
        sp.setGiaBan(110000);
        sp.setSoLuongTon(30);

        SanPham saved = vendorService.saveShopProduct(1, sp, 1, null, "NXB Trẻ", "Nguyễn Nhật Ánh");

        assertNotNull(saved);
        assertNotNull(saved.getSanPhamTacGias());
        assertEquals(1, saved.getSanPhamTacGias().size());
        assertEquals("Nguyễn Nhật Ánh", saved.getSanPhamTacGias().get(0).getTacGia().getTenTacGia());
        verify(tacGiaRepository, times(1)).save(any(vn.bookstore.the4bookstore.entity.TacGia.class));
        verify(sanPhamTacGiaRepository, times(1)).save(any(vn.bookstore.the4bookstore.entity.SanPhamTacGia.class));
    }

    @Test
    @DisplayName("Vendor nhập nhiều tác giả cách nhau bằng dấu phẩy: Hệ thống tách và tạo liên kết đầy đủ")
    void testSaveShopProduct_WithMultipleAuthors_LinksAll() {
        vn.bookstore.the4bookstore.entity.TacGia tg1 = new vn.bookstore.the4bookstore.entity.TacGia();
        tg1.setMaTacGia(1);
        tg1.setTenTacGia("Tô Hoài");
        when(tacGiaRepository.findByTenTacGiaIgnoreCase("Tô Hoài")).thenReturn(Optional.of(tg1));

        when(tacGiaRepository.findByTenTacGiaIgnoreCase("Nam Cao")).thenReturn(Optional.empty());
        when(tacGiaRepository.save(any(vn.bookstore.the4bookstore.entity.TacGia.class))).thenAnswer(invocation -> {
            vn.bookstore.the4bookstore.entity.TacGia tg = invocation.getArgument(0);
            tg.setMaTacGia(2);
            return tg;
        });

        SanPham sp = new SanPham();
        sp.setTenSP("Tuyển Tập Truyện Ngắn");
        sp.setGiaBan(95000);
        sp.setSoLuongTon(20);

        SanPham saved = vendorService.saveShopProduct(1, sp, 1, null, "NXB Văn Học", "Tô Hoài, Nam Cao");

        assertNotNull(saved);
        assertEquals(2, saved.getSanPhamTacGias().size());
        verify(sanPhamTacGiaRepository, times(2)).save(any(vn.bookstore.the4bookstore.entity.SanPhamTacGia.class));
    }
}
