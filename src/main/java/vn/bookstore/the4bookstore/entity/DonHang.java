package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DON_HANG", indexes = {
    @Index(name = "idx_dh_trangthai", columnList = "trangThai"),
    @Index(name = "idx_dh_ngaydat", columnList = "ngayDat"),
    @Index(name = "idx_dh_makh", columnList = "maKH")
})
@Data @NoArgsConstructor @AllArgsConstructor
public class DonHang {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDH;
    @Column(nullable = false)
    private LocalDateTime ngayDat = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "maKH", nullable = false)
    private KhachHang khachHang;
    
    @Column(nullable = false, length = 20)
    private String soDienThoaiGiao;
    @Column(nullable = false, length = 255)
    private String diaChiGiao;
    @Column(nullable = false)
    private Integer tongTien = 0;
    
    @ManyToOne
    @JoinColumn(name = "maKM")
    private KhuyenMai khuyenMai;
    
    @Column(nullable = false)
    private Integer tienGiam = 0;
    @Column(nullable = false, length = 30)
    private String trangThai = "ChoXuLy";
    @Column(length = 500)
    private String lyDoTuChoi;
    @Column(length = 500)
    private String lyDoHuy;
    private LocalDateTime ngayXacNhan;
    private LocalDateTime ngayHoanThanh;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_shop")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nvc")
    private NhaVanChuyen nhaVanChuyen;

    @Column(name = "phi_van_chuyen", nullable = false)
    private Integer phiVanChuyen = 0;

    @Column(name = "chiet_khau_app_phan_tram", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal chietKhauAppPhanTram = new java.math.BigDecimal("5.00");

    @Column(name = "tien_phi_san", nullable = false)
    private Integer tienPhiSan = 0;

    @Column(name = "tien_thuc_nhan_shop", nullable = false)
    private Integer tienThucNhanShop = 0;

    @Column(name = "phuong_thuc_thanh_toan", nullable = false, length = 30)
    private String phuongThucThanhToan = "COD"; // COD, VNPAY, MOMO

    @Column(name = "trang_thai_thanh_toan", nullable = false, length = 30)
    private String trangThaiThanhToan = "ChuaThanhToan"; // ChuaThanhToan, DaThanhToan, DaHoanTien

    @Column(name = "lyDoTraHang", columnDefinition = "TEXT")
    private String lyDoTraHang;

    @Column(name = "phanQuyetTranhChap", columnDefinition = "TEXT")
    private String phanQuyetTranhChap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xu_ly_tranh_chap")
    private TaiKhoan nguoiXuLyTranhChap;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL)
    private List<ChiTietDonHang> chiTietDonHangs;
}