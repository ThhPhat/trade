package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi_giao_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiGiaoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_dia_chi")
    private Integer maDiaChi;

    @ManyToOne
    @JoinColumn(name = "makh", nullable = false)
    private KhachHang khachHang;

    @Column(name = "ten_nguoi_nhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi_chi_tiet", nullable = false, length = 255)
    private String diaChiChiTiet;

    @Column(name = "phuong_xa", length = 100)
    private String phuongXa;

    @Column(name = "quan_huyen", length = 100)
    private String quanHuyen;

    @Column(name = "tinh_thanh", nullable = false, length = 100)
    private String tinhThanh;

    @Column(name = "la_mac_dinh", nullable = false)
    private Boolean laMacDinh = false;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    public String getDiaChiDayDu() {
        StringBuilder sb = new StringBuilder(diaChiChiTiet != null ? diaChiChiTiet : "");
        if (phuongXa != null && !phuongXa.isBlank()) sb.append(", ").append(phuongXa);
        if (quanHuyen != null && !quanHuyen.isBlank()) sb.append(", ").append(quanHuyen);
        if (tinhThanh != null && !tinhThanh.isBlank()) sb.append(", ").append(tinhThanh);
        return sb.toString();
    }
}
