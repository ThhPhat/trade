package vn.bookstore.the4bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChiTietPhieuNhapRequest {
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer maSP;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    @NotNull(message = "Đơn giá nhập không được để trống")
    @Min(value = 0, message = "Đơn giá nhập không được âm")
    private Integer donGiaNhap;
}
