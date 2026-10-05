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
    
    @ManyToOne
    @JoinColumn(name = "maKH", nullable = false)
    private KhachHang khachHang;
    
    @ManyToOne
    @JoinColumn(name = "maSP", nullable = false)
    private SanPham sanPham;
    
    @ManyToOne
    @JoinColumn(name = "maDH", nullable = false)
    private DonHang donHang;
    
    @Column(nullable = false)
    private Integer soSao;
    @Column(length = 1000)
    private String noiDung;
    @Column(nullable = false)
    private LocalDateTime ngayDanhGia = LocalDateTime.now();
    @Column(nullable = false, length = 20)
    private String trangThai = "HienThi";

    @OneToMany(mappedBy = "danhGia", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<DanhGiaMedia> mediaList = new java.util.ArrayList<>();
}