package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SAN_PHAM", indexes = {
    @Index(name = "idx_sp_loaisp_trangthai", columnList = "loaiSP, trangThai"),
    @Index(name = "idx_sp_danhmuc_trangthai", columnList = "maDanhMuc, trangThai"),
    @Index(name = "idx_sp_tensp", columnList = "tenSP"),
    @Index(name = "idx_sp_ngaytao", columnList = "ngayTao"),
    @Index(name = "idx_sp_giaban", columnList = "giaBan")
})
@Data @NoArgsConstructor @AllArgsConstructor
public class SanPham {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maSP;
    @Column(nullable = false, length = 200)
    private String tenSP;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maDanhMuc", nullable = false)
    private DanhMuc danhMuc;
    
    @Column(name = "ten_nha_xuat_ban", length = 150)
    private String tenNhaXuatBan;
    
    @Column(nullable = false, length = 30)
    private String loaiSP = "SACH";
    @Column(unique = true, length = 20)
    private String ISBN;
    @Column(nullable = false)
    private Integer giaBan;
    @Column(nullable = false)
    private Integer soLuongTon = 0;
    @Column(nullable = false)
    private Integer mucTonToiThieu = 0;
    @Column(length = 1000)
    private String moTa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_shop")
    private Shop shop;

    @Column(name = "trang_thai_khoa", nullable = false, length = 30)
    private String trangThaiKhoa = "BinhThuong"; // BinhThuong, BiKhoaBoiAdmin

    @Column(name = "so_luong_da_ban", nullable = false)
    private Integer soLuongDaBan = 0;

    @Column(name = "hinh_anh", length = 500)
    private String hinhAnh;

    @Column(nullable = false, length = 20)
    private String trangThai = "DangBan";
    @Column(nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<SanPhamTacGia> sanPhamTacGias = new java.util.ArrayList<>();

    public String getDanhSachTacGia() {
        try {
            if (sanPhamTacGias == null || !org.hibernate.Hibernate.isInitialized(sanPhamTacGias) || sanPhamTacGias.isEmpty()) {
                return "";
            }
            return sanPhamTacGias.stream()
                    .filter(sptg -> sptg != null && org.hibernate.Hibernate.isInitialized(sptg) && sptg.getTacGia() != null && org.hibernate.Hibernate.isInitialized(sptg.getTacGia()))
                    .map(sptg -> sptg.getTacGia().getTenTacGia())
                    .filter(name -> name != null && !name.isBlank())
                    .collect(java.util.stream.Collectors.joining(", "));
        } catch (Exception e) {
            return "";
        }
    }

    public static class NxbProxy {
        private final String tenNXB;
        private final Integer maNXB;
        public NxbProxy(String tenNXB, Integer maNXB) {
            this.tenNXB = tenNXB;
            this.maNXB = maNXB != null ? maNXB : 1;
        }
        public String getTenNXB() { return tenNXB; }
        public Integer getMaNXB() { return maNXB; }
    }

    public NxbProxy getNhaXuatBan() {
        if (tenNhaXuatBan != null && !tenNhaXuatBan.isBlank()) {
            return new NxbProxy(tenNhaXuatBan, 1);
        }
        return null;
    }

    public void setNhaXuatBan(NhaXuatBan nxb) {
        if (nxb != null) {
            this.tenNhaXuatBan = nxb.getTenNXB();
        }
    }

    public void setNhaCungCap(NhaCungCap ncc) {
        // No-op for marketplace
    }

    public NhaCungCap getNhaCungCap() {
        return null;
    }

    public String getTenNhaXuatBan() {
        return tenNhaXuatBan != null ? tenNhaXuatBan : "";
    }

    public String getTenShop() {
        try {
            if (shop != null && org.hibernate.Hibernate.isInitialized(shop)) {
                return shop.getTenShop() != null ? shop.getTenShop() : "";
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public String getShopSlug() {
        try {
            if (shop != null && org.hibernate.Hibernate.isInitialized(shop)) {
                return shop.getSlug() != null ? shop.getSlug() : "";
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public String getHinhAnh() {
        if (hinhAnh != null && !hinhAnh.isBlank()) {
            return hinhAnh;
        }
        if (moTa != null && moTa.startsWith("http")) {
            return moTa;
        }
        return "/images/default-book.png";
    }
}