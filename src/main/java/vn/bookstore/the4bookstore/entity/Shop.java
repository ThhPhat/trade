package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_shop")
    private Integer maShop;

    @OneToOne
    @JoinColumn(name = "ma_tai_khoan", nullable = false, unique = true)
    private TaiKhoan taiKhoan;

    @Column(name = "ten_shop", nullable = false, unique = true, length = 150)
    private String tenShop;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(length = 500)
    private String logo;

    @Column(length = 500)
    private String banner;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "dia_chi_shop", length = 255)
    private String diaChiShop;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "email_shop", length = 100)
    private String emailShop;

    @Column(name = "chiet_khau_phan_tram", precision = 5, scale = 2)
    private BigDecimal chietKhauPhanTram; // Tỷ lệ chiết khấu sàn riêng cho shop, nếu null lấy mặc định

    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai = "ChoDuyet"; // ChoDuyet, HoatDong, TamKhoa, BiKhoa

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SanPham> sanPhams;
}
