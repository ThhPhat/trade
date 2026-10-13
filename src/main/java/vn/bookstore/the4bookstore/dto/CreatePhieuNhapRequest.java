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
public class CreatePhieuNhapRequest {
    @NotNull(message = "Kho không được để trống")
    private Long khoId;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long nhaCungCapId;

    private Long nhanVienId;

    @NotNull(message = "Chi tiết phiếu nhập không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 sản phẩm")
    private List<PhieuNhapCTDTO> chiTiets;
}
