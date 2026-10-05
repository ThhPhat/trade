package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nha_van_chuyen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhaVanChuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nvc")
    private Integer maNvc;

    @Column(name = "ten_nvc", nullable = false, length = 100)
    private String tenNvc;

    @Column(name = "phi_co_ban", nullable = false)
    private Integer phiCoBan = 25000;

    @Column(name = "thoi_gian_du_kien", nullable = false, length = 50)
    private String thoiGianDuKien = "2 - 3 ngày";

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "HoatDong";
}
