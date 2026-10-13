package vn.bookstore.the4bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationDTO {
    @NotNull(message = "Mã đơn hàng không được để trống")
    private Integer maDH;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;

    @NotBlank(message = "Địa chỉ giao không được để trống")
    private String diaChiGiao;

    @NotNull(message = "Tổng tiền không được để trống")
    @Min(value = 0, message = "Tổng tiền không được âm")
    private Integer tongTien;

    @NotNull(message = "Số lượng món không được để trống")
    @Min(value = 1, message = "Số lượng món phải lớn hơn 0")
    private Integer soLuongMon;

    @NotBlank(message = "Trạng thái không được để trống")
    private String trangThai;

    private LocalDateTime ngayDat;
    private String message;
}
