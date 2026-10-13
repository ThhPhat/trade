package vn.bookstore.the4bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhieuKeCTDTO {
    @NotNull(message = "Sản phẩm không được để trống")
    private Long sachId;

    @NotNull(message = "Số lượng thực tế không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer soLuongThucTe;
}
