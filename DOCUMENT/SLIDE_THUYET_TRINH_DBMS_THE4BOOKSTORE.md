# SLIDE THUYẾT TRÌNH BẢO VỆ ĐỒ ÁN HỆ QUẢN TRỊ CSDL (DBMS330284)
**ĐỀ TÀI: XÂY DỰNG CƠ SỞ DỮ LIỆU SÀN THƯƠNG MẠI ĐIỆN TỬ SÁCH VÀ ỨNG DỤNG KẾT NỐI VỚI MICROSOFT SQL SERVER**
*Hệ thống: The4BookStore - Multi-Vendor Book Marketplace*

---

## SLIDE 1: TRANG TIÊU ĐỀ & GIỚI THIỆU
- **Tên đề tài:** Thiết kế & Cài đặt Cơ sở dữ liệu Sàn Thương mại Điện tử Sách (The4BookStore) trên Microsoft SQL Server.
- **Học phần:** Hệ quản trị Cơ sở dữ liệu (DBMS330284) - Khoa CNTT - ĐH Sư phạm Kỹ thuật TP.HCM / Công nghệ Kỹ thuật TP.HCM.
- **Môi trường CSDL cục bộ (Bảo vệ đồ án):** Microsoft SQL Server 2022/2025 (`localhost:1433`, CSDL: `QL_NhaSach`).
- **Ứng dụng Web cục bộ:** `http://localhost:8080/`
- **Đường dẫn trải nghiệm trực tuyến (Live Cloud Showcase):** [https://the4bookstore-phor.onrender.com/](https://the4bookstore-phor.onrender.com/)

---

## SLIDE 2: ĐẶT VẤN ĐỀ & CHUYỂN ĐỔI MÔ HÌNH MARKETPLACE
- **Bất cập của mô hình cũ (Nhà sách đơn lẻ - Single Store):**
  - Tồn tại kho tổng tập trung cồng kềnh, quy trình cấp hàng nội bộ gây nghẽn.
  - Phụ thuộc bảng Nhà cung cấp (`nha_cung_cap`) và danh mục Nhà xuất bản cố định.
  - Khuyến mãi vi phạm dạng chuẩn 1NF (lưu chuỗi ID `1,2,3`).
- **Giải pháp - Chuyển sang Sàn thương mại điện tử trung gian (Multi-vendor Marketplace):**
  - **Bỏ kho tổng:** Gian hàng (Shop) tự quản lý và tự thực hiện kiểm kê kho riêng (`phieu_kiem_ke` thuộc `ma_shop`).
  - **Bỏ ràng buộc cứng NXB:** Tên nhà xuất bản là trường text độc lập (`ten_nha_xuat_ban`) trên sách.
  - **Chuẩn hóa Khuyến mãi 1NF:** Chia tách rõ ràng Voucher Toàn Sàn (`TOAN_SAN`) và Voucher Shop (`SHOP`).
  - **Kinh doanh đa đối tác:** Sàn thu phí hoa hồng chiết khấu (`tien_phi_san`) trên từng đơn hàng thành công.

---

## SLIDE 3: KIẾN TRÚC DỮ LIỆU & CHUẨN HÓA 3NF
- **Quy mô CSDL:** 22 bảng dữ liệu cốt lõi kết hợp chặt chẽ.
- **Chứng minh chuẩn hóa:**
  - **1NF:** Tất cả thuộc tính nguyên tố, loại bỏ hoàn toàn các chuỗi mảng; bảng quan hệ N-N tách riêng (`chi_tiet_don_hang`, `chi_tiet_kiem_ke`, `chi_tiet_gio_hang`, `san_pham_tac_gia`).
  - **2NF:** Mọi thuộc tính không khóa phụ thuộc đầy đủ vào toàn bộ khóa chính ghép.
  - **3NF:** Loại bỏ phụ thuộc bắc cầu; dữ liệu Shop, Khách hàng, Đơn hàng được module hóa độc lập.
- **Cấu trúc thực thể cốt lõi:**
  - `tai_khoan` 1-1 `khach_hang`, `shop`, `nhan_vien`
  - `shop` 1-N `san_pham` 1-N `chi_tiet_don_hang` N-1 `don_hang`
  - `shop` 1-N `phieu_kiem_ke` 1-N `chi_tiet_kiem_ke`

---

## SLIDE 4: RÀNG BUỘC TOÀN VẸN NGHIỆP VỤ (CONSTRAINTS)
- `CK_sanpham_giaban`: Giá bán sách luôn dương (`gia_ban > 0`).
- `CK_sanpham_tonkho`: Tồn kho không âm (`so_luong_ton >= 0`).
- `CK_donhang_chietkhau`: Tỷ lệ hoa hồng sàn hợp lệ từ 0% đến 100% (`chiet_khau_app_phan_tram BETWEEN 0 AND 100`).
- `CK_km_ngay`: Ngày kết thúc voucher phải sau ngày bắt đầu (`ngay_ket_thuc >= ngay_bat_dau`).
- `UQ_danhgia_donhang_sanpham`: Ràng buộc mỗi đơn hàng chỉ được đánh giá 1 lần duy nhất trên từng cuốn sách đã mua `UNIQUE (madh, masp)`.

---

## SLIDE 5: 5 TRIGGERS T-SQL NGHIỆP VỤ NÂNG CAO
1. **`trg_ChiTietDonHang_TruTonKho`:** Kiểm tra tồn kho của shop và tự động trừ tồn kho, tăng số lượng bán khi khách đặt hàng.
2. **`trg_DonHang_HoanTonKhoKhiHuy`:** Tự động hoàn lại số lượng tồn kho sách cho shop khi đơn hàng chuyển sang trạng thái `'DaHuy'`.
3. **`trg_PhieuKiemKe_CanBangTonKho`:** Tự động cập nhật `so_luong_ton` bằng đúng `so_luong_thuc_te` khi shop duyệt phiếu kiểm kê `'DaKiemKe'`.
4. **`trg_DanhGia_KiemTraDaMuaHang`:** Chặn đánh giá ảo; bắt buộc đơn hàng phải ở trạng thái `'GiaoThanhCong'` hoặc `'HoanTat'`.
5. **`trg_TaiKhoan_GhiNhatKyBaoMat`:** Ghi vết tự động vào `nhat_ky_bao_mat` khi tài khoản thay đổi mật khẩu hoặc trạng thái khóa/mở khóa.

---

## SLIDE 6: 5 VIEWS BÁO CÁO QUẢN TRỊ & DOANH THU SÀN
1. **`vw_BaoCaoDoanhThuTheoThang`:** Tổng hợp doanh thu, phí sàn thu được, và số tiền giải ngân toàn sàn theo từng tháng.
2. **`vw_DoanhThuTungShop`:** Báo cáo doanh số bán, phí sàn đã nộp, và số tiền thực nhận của từng gian hàng đối tác.
3. **`vw_TopSachBanChayToanSan`:** Xếp hạng các đầu sách bán chạy nhất toàn sàn kèm thông tin shop bán.
4. **`vw_CanhBaoTonKhoShop`:** Cảnh báo các đầu sách chạm mức tồn tối thiểu (`HET_HANG`, `SAP_HET_HANG`) cho từng shop.
5. **`vw_ThongKeDanhGiaSanPham`:** Thống kê số lượt review, điểm sao trung bình, và tỷ lệ đánh giá 5 sao.

---

## SLIDE 7: 5 INDEXES & PHÂN TÍCH HIỆU NĂNG TRUY VẤN
- **Danh mục Index tối ưu:**
  1. `IX_sanpham_shop_trangthai`: Covered index tìm kiếm sách theo shop và trạng thái.
  2. `IX_donhang_shop_ngaydat`: Tăng tốc lọc đơn hàng của shop theo thời gian giảm dần.
  3. `IX_sanpham_danhmuc_giaban`: Phục vụ duyệt sách theo danh mục và sắp xếp theo giá.
  4. `IX_khuyenmai_code_thoigian`: Tối ưu kiểm tra voucher khi thanh toán.
  5. `IX_danhgia_masp_sao`: Tăng tốc tính điểm sao trung bình của sản phẩm.
- **Minh chứng Execution Plan:**
  - Trước khi có Index: Thao tác quét toàn bảng `Clustered Index Scan` (Chi phí Cost 100%).
  - Sau khi có Covered Index: Thao tác tìm kiếm trực tiếp `Index Seek` (Chi phí Cost < 2%), loại bỏ Bookmark Lookup, tốc độ truy vấn tăng gấp nhiều lần.

---

## SLIDE 8: LẬP TRÌNH CSDL - 5 STORED PROCEDURES
- Sử dụng triệt để `BEGIN TRANSACTION` và khối `TRY...CATCH` đảm bảo tính nguyên tử:
  1. **`sp_DatHangVaTruTonKho`:** Khóa dòng sản phẩm, kiểm tra tồn kho, trừ kho, tạo đơn hàng, chi tiết đơn và giao dịch thanh toán trong một transaction duy nhất.
  2. **`sp_HuyDonHang`:** Hủy đơn hàng an toàn, chặn hủy đơn đang giao, tự động hoàn kho và hoàn lượt voucher.
  3. **`sp_ShopXacNhanKiemKe`:** Gian hàng chốt sổ kiểm kê và kích hoạt cân bằng kho.
  4. **`sp_ShopXacNhanDonHang`:** Gian hàng tiếp nhận đơn và gán mã vận đơn bưu cục (`ma_van_don`).
  5. **`sp_TimKiemSachPhanTrang`:** Tìm kiếm sách đa tiêu chí có phân trang bằng `OFFSET ... ROWS FETCH NEXT ... ROWS ONLY`.

---

## SLIDE 9: 5 USER-DEFINED FUNCTIONS (UDF)
1. **`fn_TinhDiemDanhGiaTrungBinh` (Scalar UDF):** Tính điểm đánh giá trung bình làm tròn 1 chữ số thập phân cho cuốn sách.
2. **`fn_TinhPhiSanDonHang` (Scalar UDF):** Tính phí hoa hồng sàn thu từ đơn hàng dựa vào tỷ lệ chiết khấu của shop.
3. **`fn_KiemTraVoucherHopLe` (Scalar UDF):** Kiểm tra điều kiện áp mã (thời hạn, đơn tối thiểu, số lượt còn lại).
4. **`fn_LayDanhSachSachCuaShop` (Inline TVF):** Trả về bảng danh sách sách đang hoạt động của shop kèm điểm đánh giá.
5. **`fn_LayLichSuDonHangKhachHang` (Inline TVF):** Trả về bảng lịch sử các đơn hàng của một khách hàng cụ thể.

---

## SLIDE 10: XỬ LÝ ĐỒNG THỜI & TRANH CHẤP DỮ LIỆU (CONCURRENCY)
- **Vấn đề tranh chấp thực tế:** **Lost Update / Bán vượt tồn kho (Overselling)** khi 2 khách hàng cùng đặt mua cuốn sách cuối cùng tại cùng 1 giây.
- **Giải pháp kỹ thuật T-SQL:** Áp dụng **Pessimistic Locking (Khóa bi quan)**:
  ```sql
  SELECT so_luong_ton 
  FROM san_pham WITH (UPDLOCK, ROWLOCK) 
  WHERE masp = @masp;
  ```
- **Kịch bản Demo trên 2 Session SSMS:**
  - *Session 1:* Giữ khóa `UPDLOCK` trong 8 giây (`WAITFOR DELAY '00:00:08'`).
  - *Session 2:* Cùng đọc và mua cuốn sách đó. Session 2 bị chặn (Blocked) an toàn, chờ Session 1 hoàn tất mới đọc lại tồn kho thực tế (= 0) và báo hết hàng, ngăn chặn hoàn toàn Lost Update.

---

## SLIDE 11: BẢO MẬT & PHÂN QUYỀN HỆ THỐNG (4 ROLES)
- **Thiết kế theo nguyên tắc đặc quyền tối thiểu (Least Privilege):**
  1. **`Role_Admin`:** Quyền quản trị tối cao, kiểm soát toàn diện CSDL và phân quyền.
  2. **`Role_Vendor`:** Toàn quyền trên sản phẩm, đơn hàng và phiếu kiểm kê của shop; **`DENY`** can thiệp bảng tài khoản và danh mục chung.
  3. **`Role_NhanVienSan`:** Duyệt shop, quản lý danh mục, tra cứu vận đơn, xem báo cáo doanh số; **`DENY DELETE`** đơn hàng và gian hàng.
  4. **`Role_KhachHang`:** Quyền xem sản phẩm, quản lý giỏ hàng cá nhân, đặt hàng và gửi đánh giá sản phẩm đã mua.

---

## SLIDE 12: KẾT NỐI ỨNG DỤNG (SPRING BOOT + SQL SERVER)
- **Công nghệ nền tảng:** Java 17/22, Spring Boot 4, Spring Data JPA, Thymeleaf, Tailwind CSS.
- **Kết nối CSDL:**
  - Driver: `com.microsoft.sqlserver.jdbc.SQLServerDriver`
  - Dialect: `org.hibernate.dialect.SQLServerDialect`
  - HikariCP Connection Pool: Quản lý kết nối tối ưu với ngưỡng 12 connections, tự động giải phóng idle.
- **Thực thi Stored Procedure & UDF:** Gọi trực tiếp từ Spring Data Repositories thông qua Native Queries.

---

## SLIDE 13: KẾT QUẢ THỰC NGHIỆM & KỊCH BẢN DEMO
1. **Khách hàng (`khachhang_an`):**
   - Đăng nhập, xem sách *Mắt Biếc*, thêm vào giỏ hàng.
   - Đặt hàng thành công đơn #5: Tự động tính phí sàn 5%, trừ tồn kho từ 49 xuống 47.
2. **Kiểm thử Trigger tự động hoàn tồn kho:**
   - Hủy đơn hàng #2 (đang chờ duyệt): Trigger tự động hoàn trả số lượng sách *Rừng Na Uy* từ 39 lên 40 và *Clean Code* từ 19 lên 20.
3. **Kiểm thử Trigger chặn hủy sai quy trình:**
   - Thử hủy đơn #5 (đã xác nhận đang giao): Hệ thống từ chối và báo lỗi nghiệp vụ.
4. **Gian hàng (`vendor_kimdong`):**
   - Đăng nhập kênh Vendor, kiểm tra tồn kho, xác nhận đơn hàng qua SP gán mã vận đơn bưu cục.

---

## SLIDE 14: KẾ HOẠCH SAO LƯU & PHỤC HỒI DỮ LIỆU (.BAK)
- **Tập tin sao lưu chuẩn:** `DOCUMENT/scripts/QL_NhaSach.bak` (Dung lượng: ~8.37 MB).
- **Loại sao lưu:** Full Database Backup chứa đầy đủ 22 bảng, triggers, procedures, views, functions, roles và dữ liệu thử nghiệm.
- **Lệnh phục hồi (Restore) 1-click trong SSMS:**
  ```sql
  RESTORE DATABASE QL_NhaSach
  FROM DISK = N'C:\Users\Phat\the4BookStore\DOCUMENT\scripts\QL_NhaSach.bak'
  WITH REPLACE, RECOVERY;
  ```

---

## SLIDE 15: KẾT LUẬN & ĐÁNH GIÁ CHUẨN ĐẦU RA (CLO)
- **CLO1:** Vận dụng toàn diện T-SQL, Stored Procedures, Triggers, Views, Indexes vào bài toán Sàn thương mại điện tử thực tế.
- **CLO2:** Nghiên cứu và hiện thực hóa kết nối ứng dụng Spring Boot với SQL Server ổn định, an toàn.
- **CLO3:** Bố trí kiến trúc CSDL đạt chuẩn 3NF, giải quyết tranh chấp đồng thời và phân quyền chặt chẽ.
- **CLO4:** Cung cấp giải pháp triển khai song song: CSDL SQL Server cục bộ phục vụ bảo vệ đồ án và bản Live Web 24/7 trên Render ([https://the4bookstore-phor.onrender.com/](https://the4bookstore-phor.onrender.com/)).

**CẢM ƠN THẦY CÔ VÀ HỘI ĐỒNG ĐÃ LẮNG NGHE!**
*(Sẵn sàng bước vào phần Vấn đáp & Demo trực tiếp)*
