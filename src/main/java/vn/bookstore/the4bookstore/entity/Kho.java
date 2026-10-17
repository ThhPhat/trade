package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "KHO")
@Data @NoArgsConstructor @AllArgsConstructor
public class Kho {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKho;
    @Column(nullable = false, length = 100)
    private String tenKho;
    @Column(length = 255)
    private String diaChi;
    @Column(length = 20)
    private String soDienThoai;
    @Column(nullable = false, length = 20)
    private String trangThai = "HoatDong";
    @Column(nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();
    @Column(length = 500)
    private String ghiChu;

    @OneToMany(mappedBy = "kho")
    private List<KhoHang> danhSachKhoHang;

    @OneToMany(mappedBy = "kho")
    private List<PhieuNhap> danhSachPhieuNhap;
}

