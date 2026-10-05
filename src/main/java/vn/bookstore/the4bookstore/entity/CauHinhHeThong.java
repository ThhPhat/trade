package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cau_hinh_he_thong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CauHinhHeThong {

    @Id
    @Column(name = "ma_cau_hinh", length = 50)
    private String maCauHinh;

    @Column(name = "gia_tri", nullable = false)
    private String giaTri;

    @Column(name = "mo_ta")
    private String moTa;
}
