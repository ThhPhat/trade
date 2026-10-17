package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "phieu_kiem_ke", indexes = {
    @Index(name = "idx_pkk_ngaykiemke", columnList = "ngay_kiem_ke"),
    @Index(name = "idx_pkk_trangthai", columnList = "trang_thai")
})
@Data @NoArgsConstructor @AllArgsConstructor
public class PhieuKiemKe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieu_kiem_ke")
    private Integer maPhieuKiemKe;

    @Column(name = "ngay_kiem_ke", nullable = false)
    private LocalDateTime ngayKiemKe = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_shop", nullable = false)
    private Shop shop;

    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai = "DangKiemKe";

    @Column(name = "ngay_phe_duyet")
    private LocalDateTime ngayPheDuyet;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @OneToMany(mappedBy = "phieuKiemKe")
    private List<ChiTietKiemKe> chiTietKiemKes;
}