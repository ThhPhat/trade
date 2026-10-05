package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham_da_xem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDaXem {

    @EmbeddedId
    private SanPhamDaXemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maKH")
    @JoinColumn(name = "makh")
    @ToString.Exclude
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSP")
    @JoinColumn(name = "masp")
    @ToString.Exclude
    private SanPham sanPham;

    @Column(name = "thoi_gian_xem", nullable = false)
    private LocalDateTime thoiGianXem = LocalDateTime.now();
}
