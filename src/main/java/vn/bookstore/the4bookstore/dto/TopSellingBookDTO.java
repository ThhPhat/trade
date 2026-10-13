package vn.bookstore.the4bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingBookDTO {
    @NotNull(message = "Mã sách không được để trống")
    private Long sachId;

    @NotBlank(message = "Tên sách không được để trống")
    private String tenSach;

    @NotNull(message = "Số lượng bán không được để trống")
    @Min(value = 0, message = "Số lượng bán không được âm")
    private Long soLuongBan;

    @Min(value = 0, message = "Giá bán không được âm")
    private Integer giaBan;

    @Min(value = 0, message = "Doanh thu đóng góp không được âm")
    private Long doanhThuDongGop;

    public TopSellingBookDTO(Long sachId, String tenSach, Long soLuongBan) {
        this.sachId = sachId;
        this.tenSach = tenSach;
        this.soLuongBan = soLuongBan;
        this.giaBan = null;
        this.doanhThuDongGop = null;
    }

}

