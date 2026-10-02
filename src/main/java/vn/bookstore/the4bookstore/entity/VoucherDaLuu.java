package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng trung gian lưu voucher mà người dùng đã "thu thập" từ Kho chung.
 * Mỗi khách hàng phải bấm "Lưu mã" thì voucher mới vào ví cá nhân.
 */
@Entity
@Table(name = "VOUCHER_DA_LUU", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"maKH", "maKM"})
})
@Data @NoArgsConstructor @AllArgsConstructor
public class VoucherDaLuu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKH", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maKM", nullable = false)
    private KhuyenMai khuyenMai;

    @Column(nullable = false)
    private LocalDateTime ngayLuu = LocalDateTime.now();

    // ChuaDung, DaDung
    @Column(nullable = false, length = 20)
    private String trangThai = "ChuaDung";

    private LocalDateTime ngaySuDung;
}
