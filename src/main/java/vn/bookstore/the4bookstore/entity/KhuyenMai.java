package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KHUYEN_MAI")
@Data @NoArgsConstructor @AllArgsConstructor
public class KhuyenMai {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKM;

    @Column(nullable = false, length = 150)
    private String tenKM;

    @Column(unique = true, length = 50)
    private String maCode;

    @Column(length = 500)
    private String moTa;

    // PhanTram, TienCoDinh, Freeship
    @Column(nullable = false, length = 20)
    private String loaiGiam;

    @Column(nullable = false)
    private Integer giaTriGiam;

    private Integer giamToiDa;
    private Integer donToiThieu;
    private Integer soLuongToiDa;

    @Column(nullable = false)
    private Integer soLuongDaDung = 0;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime ngayBatDau;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime ngayKetThuc;

    // HoatDong, VoHieuHoa
    @Column(nullable = false, length = 20)
    private String trangThai = "HoatDong";

    // Khối 1: Hiển thị công khai hay mã ẩn/riêng tư
    @Column(nullable = false)
    private Boolean hienThiCongKhai = true;

    // Khối 3: Điều kiện áp dụng: ALL (tất cả), DANHMUC (danh mục cụ thể), SANPHAM (sản phẩm cụ thể)
    @Column(length = 20)
    private String apDungCho = "ALL";

    @Column(length = 500)
    private String danhSachDanhMucIds;

    @Column(length = 1000)
    private String danhSachSanPhamIds;

    @Column(length = 1000)
    private String loaiTruSanPhamIds;

    // ALL (mọi người), NEW (khách mới), VIP (thành viên VIP)
    @Column(length = 20)
    private String doiTuongKhachHang = "ALL";

    // Khối 4: Giới hạn trên mỗi khách hàng
    @Column(nullable = false)
    private Integer gioiHanMoiKhachHang = 1;

    @Column(name = "loai_khuyen_mai", nullable = false, length = 30)
    private String loaiKhuyenMai = "GIAM_GIA_SAN_PHAM"; // GIAM_GIA_SAN_PHAM, MIEN_PHI_VAN_CHUYEN

    @Column(name = "pham_vi", nullable = false, length = 20)
    private String phamVi = "TOAN_SAN"; // TOAN_SAN, SHOP

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_shop")
    private Shop shop;

    /**
     * Trạng thái tính toán tự động dựa trên thời gian và số lượng:
     * - "VoHieuHoa": Admin chủ động tắt
     * - "HetHan": Quá hạn hoặc hết lượt
     * - "SapToi": Chưa tới thời gian bắt đầu
     * - "DangDienRa": Đang trong thời gian và còn lượt dùng
     */
    public String getComputedStatus() {
        if ("VoHieuHoa".equalsIgnoreCase(trangThai) || "TamDung".equalsIgnoreCase(trangThai)) {
            return "VoHieuHoa";
        }
        LocalDateTime now = LocalDateTime.now();
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) {
            return "SapToi";
        }
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) {
            return "HetHan";
        }
        if (soLuongToiDa != null && soLuongDaDung != null && soLuongDaDung >= soLuongToiDa) {
            return "HetHan";
        }
        return "DangDienRa";
    }

    public String getStatusBadgeText() {
        switch (getComputedStatus()) {
            case "DangDienRa": return "Đang diễn ra";
            case "SapToi": return "Sắp diễn ra";
            case "HetHan": return "Đã hết hạn";
            case "VoHieuHoa": return "Đã vô hiệu hóa";
            default: return "Không xác định";
        }
    }

    public boolean isDangDienRa() {
        return "DangDienRa".equals(getComputedStatus());
    }
}