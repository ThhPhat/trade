package vn.bookstore.the4bookstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TAI_KHOAN", indexes = {
    @Index(name = "idx_tk_vaitro_trangthai", columnList = "vaiTro, trangThai")
})
@Data @NoArgsConstructor @AllArgsConstructor
public class TaiKhoan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maTaiKhoan;
    @Column(unique = true, nullable = false, length = 50)
    private String tenDangNhap;
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    @Column(nullable = true, length = 255)
    private String matKhauHash;
    @Column(nullable = false, length = 30)
    private String vaiTro;
    @Column(nullable = false, length = 20)
    private String trangThai = "HoatDong";
    @Column(length = 20)
    private String authProvider = "LOCAL";
    @Column(length = 100)
    private String providerId;
    @Column(nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion = 1;
}