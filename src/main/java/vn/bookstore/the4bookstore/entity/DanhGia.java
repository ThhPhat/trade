package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DANH_GIA", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"maKH", "maSP", "maDH"})
})
@Data @NoArgsConstructor @AllArgsConstructor
public class DanhGia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDanhGia;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKH", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KhachHang khachHang;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maSP", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SanPham sanPham;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maDH", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DonHang donHang;
    
    @Column(nullable = false)
    private Integer soSao;
    @Column(length = 1000)
    private String noiDung;
    @Column(nullable = false)
    private LocalDateTime ngayDanhGia = LocalDateTime.now();
    @Column(nullable = false, length = 20)
    private String trangThai = "HienThi";

    @Column(name = "hinh_anh_danh_gia", length = 500)
    private String hinhAnhDanhGia;

    @OneToMany(mappedBy = "danhGia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<DanhGiaMedia> mediaList = new java.util.ArrayList<>();
}