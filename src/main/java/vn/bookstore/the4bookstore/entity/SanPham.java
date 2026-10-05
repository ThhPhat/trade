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
    
    @ManyToOne
    @JoinColumn(name = "maDanhMuc", nullable = false)
    private DanhMuc danhMuc;
    
    @ManyToOne
    @JoinColumn(name = "maNXB")
    private NhaXuatBan nhaXuatBan;
    
    @ManyToOne
    @JoinColumn(name = "maNCC")
    private NhaCungCap nhaCungCap;
    
    @Column(nullable = false, length = 30)
    private String loaiSP;
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
    private List<SanPhamTacGia> sanPhamTacGias = new java.util.ArrayList<>();

    public String getDanhSachTacGia() {
        if (sanPhamTacGias == null || sanPhamTacGias.isEmpty()) {
            return "";
        }
        return sanPhamTacGias.stream()
                .filter(sptg -> sptg.getTacGia() != null)
                .map(sptg -> sptg.getTacGia().getTenTacGia())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    public String getTenNhaXuatBan() {
        return nhaXuatBan != null ? nhaXuatBan.getTenNXB() : "";
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