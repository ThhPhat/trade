package vn.bookstore.the4bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChiTietKiemKeRequest {
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer maSP;

    @NotNull(message = "Số lượng thực tế không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer soLuongThucTe;
}
