package vn.bookstore.the4bookstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ManagerDisputeService {

    private final SanPhamRepository sanPhamRepository;
    private final DonHangRepository donHangRepository;
    private final NhaVanChuyenRepository nhaVanChuyenRepository;
    private final CauHinhHeThongRepository cauHinhHeThongRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;

    public ManagerDisputeService(SanPhamRepository sanPhamRepository,
                                 DonHangRepository donHangRepository,
                                 NhaVanChuyenRepository nhaVanChuyenRepository,
                                 CauHinhHeThongRepository cauHinhHeThongRepository,
                                 KhuyenMaiRepository khuyenMaiRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.donHangRepository = donHangRepository;
        this.nhaVanChuyenRepository = nhaVanChuyenRepository;
        this.cauHinhHeThongRepository = cauHinhHeThongRepository;
        this.khuyenMaiRepository = khuyenMaiRepository;
    }

    // --- KIỂM DUYỆT & KHÓA SẢN PHẨM CỦA SHOP ---
    public void khoaSanPham(Integer maSP) {
        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        sp.setTrangThaiKhoa("BiKhoaBoiAdmin");
        sanPhamRepository.save(sp);
    }

    public void moKhoaSanPham(Integer maSP) {
        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        sp.setTrangThaiKhoa("BinhThuong");
        sanPhamRepository.save(sp);
    }

    // --- TRỌNG TÀI PHÁN QUYẾT TRANH CHẤP GIỮA VENDOR VÀ CUSTOMER ---
    @Transactional(readOnly = true)
    public Page<DonHang> getDisputedOrders(Pageable pageable) {
        return donHangRepository.findByTrangThaiOrderByNgayDatDesc("TranhChap", pageable);
    }

    public DonHang phanQuyetTranhChap(Integer maDH, boolean chapNhanHoanTien, String lyDo, TaiKhoan manager) {
        DonHang dh = donHangRepository.findById(maDH)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (!"TranhChap".equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái khiếu nại tranh chấp!");
        }

        dh.setNguoiXuLyTranhChap(manager);
        dh.setPhanQuyetTranhChap(lyDo);

        if (chapNhanHoanTien) {
            dh.setTrangThai("TraHangHoanTien");
            dh.setTrangThaiThanhToan("DaHoanTien");
        } else {
            // Giữ nguyên hoàn tất đơn cho shop
            dh.setTrangThai("DaGiao");
        }

        return donHangRepository.save(dh);
    }

    // --- QUẢN LÝ NHÀ VẬN CHUYỂN ---
    @Transactional(readOnly = true)
    public List<NhaVanChuyen> getAllCarriers() {
        return nhaVanChuyenRepository.findAll();
    }

    public NhaVanChuyen saveCarrier(NhaVanChuyen nvc) {
        return nhaVanChuyenRepository.save(nvc);
    }

    // --- CẤU HÌNH CHIẾT KHẤU SÀN ---
    @Transactional(readOnly = true)
    public BigDecimal getPlatformCommissionRate() {
        return cauHinhHeThongRepository.findById("PHI_SAN_MAC_DINH")
                .map(c -> new BigDecimal(c.getGiaTri()))
                .orElse(new BigDecimal("5.00"));
    }

    public void setPlatformCommissionRate(BigDecimal rate) {
        CauHinhHeThong config = cauHinhHeThongRepository.findById("PHI_SAN_MAC_DINH")
                .orElseGet(() -> new CauHinhHeThong("PHI_SAN_MAC_DINH", "5.0", "Phí sàn mặc định"));
        config.setGiaTri(rate.toString());
        cauHinhHeThongRepository.save(config);
    }

    // --- KHUYẾN MÃI TOÀN SÀN ---
    public KhuyenMai createPlatformPromotion(KhuyenMai km) {
        km.setPhamVi("TOAN_SAN");
        km.setShop(null);
        km.setTrangThai("HoatDong");
        return khuyenMaiRepository.save(km);
    }
}
