package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDaXemId implements Serializable {
    private Integer maKH;
    private Integer maSP;
}
