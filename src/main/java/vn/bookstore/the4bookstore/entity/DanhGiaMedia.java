package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "danh_gia_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DanhGiaMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_media")
    private Integer maMedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_gia", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DanhGia danhGia;

    @Column(name = "loai_media", nullable = false, length = 10)
    private String loaiMedia = "IMAGE"; // IMAGE, VIDEO

    @Column(nullable = false, length = 500)
    private String url;
}
