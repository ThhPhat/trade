package vn.bookstore.the4bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingBookDTO {
    private Long sachId;
    private String tenSach;
    private Long soLuongBan;
    private Integer giaBan;
    private Long doanhThuDongGop;

    public TopSellingBookDTO(Long sachId, String tenSach, Long soLuongBan) {
        this.sachId = sachId;
        this.tenSach = tenSach;
        this.soLuongBan = soLuongBan;
        this.giaBan = null;
        this.doanhThuDongGop = null;
    }

}

