package vn.bookstore.the4bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhieuKeRequest {
    @NotNull(message = "Kho không được để trống")
    private Long khoId;

    private Long nhanVienId;

    @NotNull(message = "Chi tiết kiểm kê không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 sản phẩm kiểm kê")
    private List<PhieuKeCTDTO> chiTiets;

    private String ghiChu;
}
