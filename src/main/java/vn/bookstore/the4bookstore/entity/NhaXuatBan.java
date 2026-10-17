package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "NHA_XUAT_BAN")
@org.hibernate.annotations.BatchSize(size = 50)
@Data @NoArgsConstructor @AllArgsConstructor
public class NhaXuatBan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNXB;
    @Column(nullable = false, length = 150)
    private String tenNXB;
    @Column(length = 255)
    private String diaChi;
    @Column(length = 20)
    private String soDienThoai;
    @Column(length = 100)
    private String email;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SanPham> sanPhams = new ArrayList<>();
}