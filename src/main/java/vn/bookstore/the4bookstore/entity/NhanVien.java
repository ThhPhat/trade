package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "NHAN_VIEN")
@Data @NoArgsConstructor @AllArgsConstructor
public class NhanVien {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNV;
    @Column(nullable = false, length = 100)
    private String hoTen;
    @Column(unique = true, nullable = false, length = 20)
    private String soDienThoai;
    @Column(length = 100)
    private String email;
    @Column(length = 255)
    private String diaChi;
    @Column(nullable = false, length = 30)
    private String chucVu;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maTaiKhoan", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;
    
    private LocalDate ngayVaoLam;
    @Column(nullable = false, length = 20)
    private String trangThai = "DangLam";
}