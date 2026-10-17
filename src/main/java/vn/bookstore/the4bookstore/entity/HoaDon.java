package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoa_don")
@Data @NoArgsConstructor @AllArgsConstructor
public class HoaDon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hd")
    private Integer maHD;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dh", nullable = false, unique = true)
    private DonHang donHang;
    
    @Column(name = "ngay_lap", nullable = false)
    private LocalDateTime ngayLap = LocalDateTime.now();
    
    @Column(name = "tong_tien_thanh_toan", nullable = false)
    private Integer tongTienThanhToan;
    
    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai = "ChuaThanhToan";
}