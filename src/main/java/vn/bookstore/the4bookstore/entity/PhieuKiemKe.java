package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PHIEU_KIEM_KE", indexes = {
    @Index(name = "idx_pkk_ngaykiemke", columnList = "ngayKiemKe"),
    @Index(name = "idx_pkk_trangthai", columnList = "trangThai")
})
@Data @NoArgsConstructor @AllArgsConstructor
public class PhieuKiemKe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maPhieuKiemKe;
    @Column(nullable = false)
    private LocalDateTime ngayKiemKe = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_shop", nullable = false)
    private Shop shop;

    @Transient
    private Kho kho;

    @Transient
    private NhanVien nhanVien;

    @Transient
    private NhanVien nvPheDuyet;

    @Column(nullable = false, length = 30)
    private String trangThai = "DangKiemKe";

    private LocalDateTime ngayPheDuyet;
    @Column(length = 500)
    private String ghiChu;

    @OneToMany(mappedBy = "phieuKiemKe")
    private List<ChiTietKiemKe> chiTietKiemKes;
}