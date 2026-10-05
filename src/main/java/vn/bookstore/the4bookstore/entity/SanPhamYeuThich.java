package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham_yeu_thich")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamYeuThich {

    @EmbeddedId
    private SanPhamYeuThichId id;

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

    @Column(name = "ngay_thich", nullable = false)
    private LocalDateTime ngayThich = LocalDateTime.now();
}
