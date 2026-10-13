package vn.bookstore.the4bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDTO {
    @NotBlank(message = "Tháng không được để trống")
    private String month;

    @NotNull(message = "Doanh thu không được để trống")
    @Min(value = 0, message = "Doanh thu không được âm")
    private BigDecimal revenue;
}
