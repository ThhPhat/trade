package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KHACH_HANG")
@Data @NoArgsConstructor @AllArgsConstructor
public class KhachHang {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKH;
    @Column(nullable = false, length = 100)
    private String hoTen;
    @Column(unique = true, nullable = true, length = 20)
    private String soDienThoai;
    @Column(length = 100)
    private String email;
    @Column(length = 255)
    private String diaChi;
    @Column(length = 500)
    private String anhDaiDien;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maTaiKhoan", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;
    
    @Column(nullable = false)
    private LocalDateTime ngayDangKy = LocalDateTime.now();
}