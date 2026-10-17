# BÁO CÁO ĐỒ ÁN CUỐI KỲ: HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU (DBMS330284)
**ĐỀ TÀI: XÂY DỰNG CƠ SỞ DỮ LIỆU SÀN THƯƠNG MẠI ĐIỆN TỬ SÁCH VÀ ỨNG DỤNG KẾT NỐI VỚI MICROSOFT SQL SERVER (THE4BOOKSTORE)**

- **Môi trường CSDL cục bộ (Bảo vệ đồ án):** Microsoft SQL Server 2022/2025 (`localhost:1433`, CSDL: `QL_NhaSach`)
- **Ứng dụng Web cục bộ:** `http://localhost:8080/`
- **Link ứng dụng triển khai trực tuyến (Live Cloud Showcase):** [https://the4bookstore-phor.onrender.com/](https://the4bookstore-phor.onrender.com/)

---

## MỤC LỤC TỔNG QUAN

1. **CHƯƠNG 1: GIỚI THIỆU BÀI TOÁN & PHÂN TÍCH YÊU CẦU**
   - 1.1. Bối cảnh chuyển đổi sang mô hình Sàn thương mại điện tử (Multi-Vendor Marketplace)
   - 1.2. Phân tích loại bỏ các thực thể dư thừa (Kho tổng, Nhà cung cấp, Tách rời Nhà xuất bản)
   - 1.3. Mô hình nghiệp vụ cốt lõi: Khách hàng - Gian hàng (Shop) - Sàn thương mại điện tử (Platform)

2. **CHƯƠNG 2: THIẾT KẾ DỮ LIỆU & CHUẨN HÓA 3NF**
   - 2.1. Mô hình thực thể kết hợp (ERD)
   - 2.2. Lược đồ quan hệ (Relational Schema) & Chứng minh chuẩn 3NF
   - 2.3. Danh mục 22 bảng dữ liệu và cấu trúc chi tiết

3. **CHƯƠNG 3: CÀI ĐẶT RÀNG BUỘC, TRIGGER, VIEW VÀ CHỈ MỤC TRÊN SQL SERVER**
   - 3.1. Các ràng buộc toàn vẹn thực tế (CHECK, UNIQUE, DEFAULT, FOREIGN KEY)
   - 3.2. Hiện thực 5 Trigger T-SQL nghiệp vụ nâng cao
   - 3.3. Hiện thực 5 View tổng hợp báo cáo và giám sát
   - 3.4. Hiện thực 5 Index và đo lường hiệu năng truy vấn (Execution Plan / Cost Reduction)

4. **CHƯƠNG 4: LẬP TRÌNH CSDL, QUẢN LÝ GIAO DỊCH VÀ PHÂN QUYỀN BẢO MẬT**
   - 4.1. Hiện thực 5 Stored Procedure có xử lý Transaction & TRY...CATCH
   - 4.2. Hiện thực 5 Function (Scalar UDF & Inline Table-Valued Function)
   - 4.3. Quản lý đồng thời (Concurrency Control): Kịch bản mô phỏng Lost Update (Tranh chấp tồn kho) trên 2 Session SSMS
   - 4.4. Bảo mật và phân quyền hệ thống: Cấu hình 4 Database Roles chuyên biệt

5. **CHƯƠNG 5: KẾT NỐI VÀ VẬN HÀNH ỨNG DỤNG THỰC TẾ (SPRING BOOT + THYMELEAF)**
   - 5.1. Cấu hình kết nối JDBC Driver & HikariCP Pool
   - 5.2. Ánh xạ thực thể Hibernate ORM với SQL Server
   - 5.3. Gọi Stored Procedure & UDF trực tiếp từ Repository/Service
   - 5.4. Kết quả kiểm thử thực nghiệm các phân hệ (Khách hàng, Gian hàng, Quản trị)

6. **CHƯƠNG 6: KẾT LUẬN & HƯỚNG DẪN SAO LƯU / PHỤC HỒI (.BAK)**
   - 6.1. Đánh giá mức độ hoàn thành theo chuẩn đầu ra môn học
   - 6.2. Hướng dẫn Restore CSDL từ file `QL_NhaSach.bak`

---

# CHƯƠNG 1: GIỚI THIỆU BÀI TOÁN & PHÂN TÍCH YÊU CẦU

## 1.1. Bối cảnh chuyển đổi sang Sàn thương mại điện tử sách (Marketplace)
Trước đây, hệ thống *The4BookStore* được xây dựng theo mô hình nhà sách đơn lẻ (Single Store) tự nhập hàng từ nhà cung cấp về một kho tổng và tự bán lẻ. Mô hình này bộc lộ nhiều điểm hạn chế trong bài toán thương mại điện tử hiện đại:
- Không thể mở rộng cho nhiều người bán (Vendors/Shops) cùng kinh doanh độc lập.
- Chi phí vận hành kho tổng cồng kềnh, quy trình cấp hàng nội bộ giữa kho tổng và gian hàng gây tắc nghẽn.
- Hệ thống khuyến mãi vi phạm dạng chuẩn 1NF (lưu chuỗi ID ngăn cách bởi dấu phẩy `1,2,3`).

**Giải pháp chuyển đổi:** Hệ thống được tái cấu trúc triệt để thành **Sàn thương mại điện tử trung gian (Multi-vendor Marketplace)**:
- Sàn cung cấp hạ tầng, cổng thanh toán, hệ thống mã giảm giá toàn sàn, và thu phí hoa hồng chiết khấu trên mỗi đơn hàng (`tien_phi_san`).
- Các Nhà xuất bản / Shop sách tự đăng ký gian hàng, tự quản lý danh mục sách, tự định giá bán và **tự thực hiện kiểm kê kho sách riêng của gian hàng**.
- Khách hàng có thể tìm kiếm sách từ nhiều gian hàng, mua sách, áp mã giảm giá sàn/shop và đánh giá sản phẩm sau khi đơn giao thành công.

## 1.2. Phân tích loại bỏ & tối ưu các bảng dư thừa
Dựa trên yêu cầu nghiệp vụ sàn thương mại điện tử:
1. **Loại bỏ bảng Nhà cung cấp (`nha_cung_cap`) và Kho tổng (`kho`, `kho_hang`):** Trong mô hình sàn, sàn không mua buôn sách từ NCC về kho tổng để bán lại; từng gian hàng tự chịu trách nhiệm về nguồn hàng và kho hàng của mình.
2. **Tối ưu Nhà xuất bản (`nha_xuat_ban`):** Chuyển trường nhà xuất bản trên sách thành chuỗi thuộc tính mở (`ten_nha_xuat_ban NVARCHAR(150)`) giúp các gian hàng linh hoạt đăng bán sách từ mọi nhà xuất bản trong nước và quốc tế mà không bị phụ thuộc vào bảng danh mục cứng.
3. **Chuẩn hóa Khuyến mãi (`khuyen_mai`):** Loại bỏ triệt để các chuỗi phi chuẩn 1NF (`danhSachSanPhamIds`, `danhSachDanhMucIds`). Hệ thống khuyến mãi chia rõ 2 phạm vi: **Voucher Toàn Sàn** (`TOAN_SAN`) và **Voucher Gian Hàng** (`SHOP` gắn với `ma_shop`).
4. **Tối ưu Đơn hàng - Hóa đơn - Thanh toán:** Gộp các thuộc tính tài chính vào `don_hang` và `thanh_toan`, liên kết trực tiếp với mã giao dịch ngân hàng / VietQR, đảm bảo tính nhất quán và không trùng lặp dữ liệu.
5. **Chống đánh giá rác (`danh_gia`):** Ràng buộc UNIQUE `(madh, masp)` và Trigger kiểm tra bắt buộc đơn hàng đã ở trạng thái `GiaoThanhCong`/`HoanTat` mới được gửi nhận xét.

---

# CHƯƠNG 2: THIẾT KẾ DỮ LIỆU & CHUẨN HÓA 3NF

## 2.1. Lược đồ quan hệ (Relational Schema)
Toàn bộ hệ thống gồm 22 bảng cốt lõi và các bảng bổ trợ được chuẩn hóa đạt mức 3NF:
1. `tai_khoan` (**ma_tai_khoan**, ten_dang_nhap, email, mat_khau_hash, vai_tro, trang_thai, auth_provider, provider_id, token_version, otp_code, otp_expiry, ngay_tao)
2. `khach_hang` (**makh**, *ma_tai_khoan*, ho_ten, email, so_dien_thoai, dia_chi, anh_dai_dien, ngay_dang_ky)
3. `dia_chi_giao_hang` (**ma_dia_chi**, *makh*, ten_nguoi_nhan, so_dien_thoai, dia_chi_chi_tiet, phuong_xa, quan_huyen, tinh_thanh, la_mac_dinh, ngay_tao)
4. `nhan_vien` (**manv**, *ma_tai_khoan*, ho_ten, so_dien_thoai, email, chuc_vu, ngay_vao_lam, trang_thai)
5. `shop` (**ma_shop**, *ma_tai_khoan*, ten_shop, slug, mo_ta, logo, dia_chi_kho, so_dien_thoai, email_shop, chiet_khau_phan_tram, trang_thai, ngay_tao, ngay_cap_nhat)
6. `danh_muc` (**ma_danh_muc**, ten_danh_muc, slug, loai_sp, ma_danh_muc_cha, trang_thai)
7. `tac_gia` (**ma_tac_gia**, ten_tac_gia, tieu_su, quoc_tich)
8. `san_pham` (**masp**, *ma_shop*, *ma_danh_muc*, tensp, ten_nha_xuat_ban, isbn, gia_ban, so_luong_ton, so_luong_da_ban, muc_ton_toi_thieu, loaisp, mo_ta, hinh_anh, trang_thai, trang_thai_khoa, ngay_tao)
9. `san_pham_tac_gia` (**\*masp\*, \*ma_tac_gia\***, thu_tu_tac_gia)
10. `phieu_kiem_ke` (**ma_phieu**, *ma_shop*, *ma_tai_khoan_nguoi_tao*, *ma_tai_khoan_nguoi_duyet*, ngay_tao, ngay_kiem_ke, trang_thai, ghi_chu)
11. `chi_tiet_kiem_ke` (**\*ma_phieu\*, \*masp\***, so_luong_he_thong, so_luong_thuc_te, chenh_lech, ly_do)
12. `nha_van_chuyen` (**ma_nvc**, ten_nvc, phi_co_ban, trang_thai)
13. `khuyen_mai` (**makm**, ma_code, tenkm, mo_ta, loai_giam, gia_tri_giam, giam_toi_da, don_toi_thieu, so_luong_toi_da, so_luong_da_dung, pham_vi, *ma_shop*, ngay_bat_dau, ngay_ket_thuc, trang_thai, hien_thi_cong_khai, gioi_han_moi_khach_hang, doi_tuong_khach_hang, loai_khuyen_mai)
14. `gio_hang` (**ma_gio_hang**, *makh*, ngay_tao, ngay_cap_nhat)
15. `chi_tiet_gio_hang` (**\*ma_gio_hang\*, \*masp\***, so_luong)
16. `don_hang` (**madh**, *makh*, *ma_shop*, *ma_nvc*, *makm*, dia_chi_giao, so_dien_thoai_giao, tong_tien, phi_van_chuyen, tien_giam, chiet_khau_app_phan_tram, tien_phi_san, tien_thuc_nhan_shop, phuong_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai, ma_van_don, ly_do_huy, ly_do_tu_choi, ly_do_tra_hang, phan_quyet_tranh_chap, *nguoi_xu_ly_tranh_chap*, ngay_dat, ngay_xac_nhan, ngay_hoan_thanh)
17. `chi_tiet_don_hang` (**\*madh\*, \*masp\***, don_gia, so_luong)
18. `thanh_toan` (**ma_thanh_toan**, *madh*, phuong_thuc, so_tien, trang_thai, ngay_thanh_toan, ma_giao_dich, noi_dung)
19. `danh_gia` (**ma_danh_gia**, *madh*, *makh*, *masp*, so_sao, noi_dung, hinh_anh_danh_gia, ngay_danh_gia)
20. `san_pham_yeu_thich` (**\*makh\*, \*masp\***, ngay_them)
21. `voucher_da_luu` (**\*makh\*, \*makm\***, ngay_luu, trang_thai, ngay_su_dung)
22. `nhat_ky_bao_mat` (**ma_nhat_ky**, ten_bang, hanh_dong, ban_ghi_id, du_lieu_cu, du_lieu_moi, nguoi_thuc_hien, thoi_gian)

## 2.2. Chứng minh đạt chuẩn 3NF
- **Chuẩn 1NF (First Normal Form):** Tất cả các thuộc tính đều là nguyên tố (atomic). Không có thuộc tính đa trị (repeating groups). Chuỗi ID danh mục/sản phẩm ngăn cách bởi dấu phẩy cũ đã bị loại bỏ hoàn toàn; bảng quan hệ nhiều-nhiều được tách riêng (`san_pham_tac_gia`, `chi_tiet_kiem_ke`, `chi_tiet_don_hang`, `chi_tiet_gio_hang`).
- **Chuẩn 2NF (Second Normal Form):** Đạt 1NF và mọi thuộc tính không khóa đều phụ thuộc hàm đầy đủ vào toàn bộ khóa chính (Full functional dependency), không phụ thuộc vào một phần của khóa chính ghép. Ví dụ trong `chi_tiet_don_hang (madh, masp)`: thuộc tính `don_gia` và `so_luong` phụ thuộc vào cả cặp `(madh, masp)`.
- **Chuẩn 3NF (Third Normal Form):** Đạt 2NF và không có phụ thuộc bắc cầu (Transitive dependency) giữa các thuộc tính không khóa. Các thông tin của Shop (tên shop, chiết khấu) được tách riêng ở bảng `shop`, đơn hàng chỉ tham chiếu khóa ngoại `ma_shop`. Bảng `khach_hang` và `tai_khoan` phân tách rõ trách nhiệm bảo mật và hồ sơ cá nhân.

---

# CHƯƠNG 3: CÀI ĐẶT RÀNG BUỘC, TRIGGER, VIEW VÀ CHỈ MỤC TRÊN SQL SERVER

## 3.1. Các ràng buộc toàn vẹn thực tế (Constraints)
1. **CHECK `CK_sanpham_giaban`:** Đảm bảo giá sách đăng bán luôn dương: `gia_ban > 0`.
2. **CHECK `CK_sanpham_tonkho`:** Không cho phép số lượng tồn âm: `so_luong_ton >= 0`.
3. **CHECK `CK_km_giatri` & `CK_km_ngay`:** Giá trị giảm giá phải lớn hơn 0, và ngày kết thúc phải sau hoặc bằng ngày bắt đầu: `ngay_ket_thuc >= ngay_bat_dau`.
4. **CHECK `CK_donhang_chietkhau`:** Tỷ lệ phần trăm hoa hồng sàn thu từ shop phải hợp lệ từ 0% đến 100%: `chiet_khau_app_phan_tram BETWEEN 0 AND 100`.
5. **UNIQUE `UQ_danhgia_donhang_sanpham`:** Mỗi khách hàng chỉ được đánh giá 1 lần duy nhất cho mỗi cuốn sách thuộc về đơn hàng cụ thể đã mua: `UNIQUE (madh, masp)`.

## 3.2. Hiện thực 5 Trigger T-SQL nghiệp vụ
1. **`trg_ChiTietDonHang_TruTonKho` (AFTER INSERT on `chi_tiet_don_hang`):**
   - *Ý nghĩa:* Tự động kiểm tra số lượng tồn kho của sách tại Shop. Nếu không đủ tồn kho, kích hoạt `RAISERROR` và `ROLLBACK TRANSACTION`. Nếu đủ, tự động trừ tồn kho và tăng số lượng đã bán.
2. **`trg_DonHang_HoanTonKhoKhiHuy` (AFTER UPDATE of `trang_thai` on `don_hang`):**
   - *Ý nghĩa:* Khi đơn hàng chuyển trạng thái sang `'DaHuy'`, trigger tự động hoàn trả lại số lượng sách vào tồn kho của shop và giảm số lượng đã bán tương ứng.
3. **`trg_PhieuKiemKe_CanBangTonKho` (AFTER UPDATE of `trang_thai` on `phieu_kiem_ke`):**
   - *Ý nghĩa:* Khi Shop xác nhận hoàn thành phiếu kiểm kê (`'DaKiemKe'`), trigger tự động cập nhật lại `so_luong_ton` trong bảng `san_pham` bằng đúng `so_luong_thuc_te` đã kiểm đếm.
4. **`trg_DanhGia_KiemTraDaMuaHang` (INSTEAD OF INSERT on `danh_gia`):**
   - *Ý nghĩa:* Chặn triệt để đánh giá ảo. Chỉ cho phép chèn đánh giá nếu đơn hàng `madh` của khách hàng đó đã ở trạng thái `'GiaoThanhCong'` hoặc `'HoanTat'`.
5. **`trg_TaiKhoan_GhiNhatKyBaoMat` (AFTER UPDATE on `tai_khoan`):**
   - *Ý nghĩa:* Giám sát an ninh hệ thống. Khi mật khẩu hoặc trạng thái tài khoản bị sửa đổi (khóa/mở khóa), trigger tự động ghi nhận vết vào bảng `nhat_ky_bao_mat` gồm người thực hiện, thời gian và dữ liệu cũ/mới.

## 3.3. Hiện thực 5 View tổng hợp báo cáo
1. **`vw_BaoCaoDoanhThuTheoThang`:** Báo cáo doanh số toàn sàn theo từng tháng: tổng đơn đặt, tổng đơn hoàn tất, tổng tiền bán, tổng phí sàn thu được, và tổng tiền chuyển về các shop.
2. **`vw_DoanhThuTungShop`:** Báo cáo doanh số lũy kế của từng gian hàng: tỷ lệ hoa hồng, số đơn thành công, tổng doanh thu và số tiền sàn cần đối soát giải ngân.
3. **`vw_TopSachBanChayToanSan`:** Xếp hạng các đầu sách bán chạy nhất toàn sàn, kèm tên gian hàng và danh mục.
4. **`vw_CanhBaoTonKhoShop`:** Cảnh báo các đầu sách sắp hết hàng (`so_luong_ton <= muc_ton_toi_thieu`) của từng shop kèm mức độ khẩn cấp (`HET_HANG` hoặc `SAP_HET_HANG`).
5. **`vw_ThongKeDanhGiaSanPham`:** Tổng hợp số lượt đánh giá, điểm sao trung bình, và số lượt đánh giá 5 sao cho từng đầu sách.

## 3.4. Hiện thực 5 Index & Phân tích hiệu năng (Execution Plan)
1. `IX_sanpham_shop_trangthai` on `san_pham (ma_shop, trang_thai) INCLUDE (tensp, gia_ban, so_luong_ton)`
2. `IX_donhang_shop_ngaydat` on `don_hang (ma_shop, ngay_dat DESC) INCLUDE (tong_tien, trang_thai)`
3. `IX_sanpham_danhmuc_giaban` on `san_pham (ma_danh_muc, gia_ban) INCLUDE (tensp, hinh_anh)`
4. `IX_khuyenmai_code_thoigian` on `khuyen_mai (ma_code, trang_thai, ngay_bat_dau, ngay_ket_thuc)`
5. `IX_danhgia_masp_sao` on `danh_gia (masp, so_sao) INCLUDE (makh, ngay_danh_gia)`

*Minh chứng cải thiện hiệu năng:* Khi thực hiện truy vấn danh mục sách của gian hàng có lọc theo trạng thái trên bảng dữ liệu lớn, việc có Covered Index `IX_sanpham_shop_trangthai` chuyển đổi thao tác từ `Clustered Index Scan` (chi phí Cost 100%) sang `Index Seek` (chi phí Cost < 2%), thời gian thực thi giảm từ hàng chục mili-giây xuống dưới 1 mili-giây mà không cần tra cứu bookmark (Key Lookup).

---

# CHƯƠNG 4: LẬP TRÌNH CSDL, QUẢN LÝ GIAO DỊCH VÀ BẢO MẬT

## 4.1. Hiện thực 5 Stored Procedure nâng cao
1. **`sp_DatHangVaTruTonKho`:**
   - Thực hiện nghiệp vụ đặt hàng nguyên tử (Atomic). Sử dụng `BEGIN TRANSACTION` và `TRY...CATCH`.
   - Kiểm tra tồn kho của từng mặt hàng bằng cú pháp khóa cấp dòng `WITH (UPDLOCK, ROWLOCK)`.
   - Tính toán phí sàn, trừ tồn kho, tạo bản ghi `don_hang`, `chi_tiet_don_hang` và tạo thanh toán `thanh_toan`. Nếu có bất kỳ lỗi nào, tự động `ROLLBACK TRANSACTION`.
2. **`sp_HuyDonHang`:**
   - Hủy đơn hàng an toàn, kiểm tra trạng thái đơn nếu đang giao thì chặn không cho hủy.
   - Khi hủy thành công, hoàn lại tồn kho và hoàn lại lượt dùng mã giảm giá.
3. **`sp_ShopXacNhanKiemKe`:**
   - Dành riêng cho gian hàng chốt sổ kiểm kê kho định kỳ. Chuyển trạng thái phiếu sang `'DaKiemKe'`, kích hoạt cân bằng tồn kho tự động.
4. **`sp_ShopXacNhanDonHang`:**
   - Shop nhận đơn, cập nhật mã vận đơn bưu cục (`ma_van_don`) và chuyển trạng thái sang `'DangGiao'`.
5. **`sp_TimKiemSachPhanTrang`:**
   - Tìm kiếm sách toàn sàn đa tiêu chí (từ khóa, danh mục, tác giả, khoảng giá, sắp xếp) có phân trang hiệu quả bằng mệnh đề `OFFSET @Offset ROWS FETCH NEXT @PageSize ROWS ONLY`.

## 4.2. Hiện thực 5 Function (UDF)
1. **`fn_TinhDiemDanhGiaTrungBinh` (Scalar UDF):** Tính điểm đánh giá trung bình làm tròn 1 chữ số thập phân cho một sản phẩm.
2. **`fn_TinhPhiSanDonHang` (Scalar UDF):** Tính số tiền hoa hồng sàn thu được từ giá trị đơn hàng và tỷ lệ chiết khấu của shop.
3. **`fn_KiemTraVoucherHopLe` (Scalar UDF):** Kiểm tra tính hợp lệ của mã voucher đối với giá trị đơn hàng, thời hạn và số lượng còn lại.
4. **`fn_LayDanhSachSachCuaShop` (Inline Table-Valued Function):** Trả về toàn bộ danh mục sách của một shop đang mở bán kèm điểm đánh giá.
5. **`fn_LayLichSuDonHangKhachHang` (Inline Table-Valued Function):** Trả về bảng lịch sử mua hàng chi tiết của khách hàng kèm tên shop và số lượng mặt hàng.

## 4.3. Quản lý đồng thời & Mô phỏng Concurrency (Lost Update)
Trong bài toán sàn thương mại điện tử, tranh chấp dữ liệu kinh điển nhất là **Lost Update / Overselling (Bán vượt tồn kho)** khi 2 khách hàng cùng đặt mua cuốn sách cuối cùng tại cùng một thời điểm:
- **Nguyên nhân:** Nếu áp dụng mức cô lập mặc định `READ COMMITTED` với câu lệnh `SELECT so_luong_ton` thông thường, cả 2 phiên (Session 1 và Session 2) cùng đọc được `so_luong_ton = 1`. Cả hai kiểm tra `1 >= 1` hợp lệ, sau đó Session 1 trừ còn 0, Session 2 ghi đè tiếp tục trừ thành -1 (hoặc gây lỗi xung đột).
- **Giải pháp giải quyết triệt để:** Sử dụng kỹ thuật **Pessimistic Locking (Khóa bi quan)** trong T-SQL thông qua gợi ý khóa:
  ```sql
  SELECT so_luong_ton 
  FROM san_pham WITH (UPDLOCK, ROWLOCK) 
  WHERE masp = @masp;
  ```
- **Kịch bản kiểm thử trên 2 cửa sổ SSMS:**
  - *Session 1:* Mở giao dịch, giữ khóa `UPDLOCK`:
    ```sql
    BEGIN TRANSACTION;
    SELECT so_luong_ton FROM san_pham WITH (UPDLOCK, ROWLOCK) WHERE masp = 1;
    WAITFOR DELAY '00:00:08'; -- Giữ khóa 8 giây mô phỏng độ trễ xử lý
    UPDATE san_pham SET so_luong_ton = so_luong_ton - 1 WHERE masp = 1;
    COMMIT TRANSACTION;
    ```
  - *Session 2:* Chạy ngay sau Session 1:
    ```sql
    BEGIN TRANSACTION;
    SELECT so_luong_ton FROM san_pham WITH (UPDLOCK, ROWLOCK) WHERE masp = 1;
    -- Session 2 sẽ tự động bị chặn (BLOCKED) an toàn cho tới khi Session 1 COMMIT.
    -- Khi Session 2 đọc được số lượng mới, số lượng đã bằng 0 và không xảy ra Lost Update.
    COMMIT TRANSACTION;
    ```

## 4.4. Bảo mật và phân quyền hệ thống (4 Roles)
Hệ thống thiết lập 4 Database Roles tuân thủ nguyên tắc đặc quyền tối thiểu (Principle of Least Privilege):
1. **`Role_Admin`:** Quyền quản trị tối cao, kiểm soát toàn bộ bảng, chạy backup/restore và phân quyền người dùng.
2. **`Role_Vendor` (Gian hàng):**
   - Chỉ được quản lý sách của mình (`SELECT, INSERT, UPDATE on san_pham`), xem và xác nhận đơn hàng của shop mình, lập phiếu kiểm kê.
   - Bị cấm sửa đổi bảng tài khoản, không được sửa danh mục chung (`DENY INSERT, UPDATE, DELETE on danh_muc, tai_khoan`).
3. **`Role_Manager` (Quản lý vận hành hệ thống):**
   - Duyệt gian hàng mới, quản lý danh mục, quản lý tác giả, tra cứu vận chuyển, giải quyết tranh chấp khiếu nại và xem báo cáo tài chính sàn.
   - Bị cấm xóa đơn hàng hoặc xóa gian hàng (`DENY DELETE on don_hang, shop`), không được sửa tài khoản Admin cấp cao (`DENY INSERT, UPDATE, DELETE on tai_khoan`).
4. **`Role_KhachHang` (Người mua):**
   - Xem sản phẩm công khai; khi đăng nhập có quyền quản lý giỏ hàng của chính mình, lưu và áp mã voucher, tạo đơn hàng và đánh giá sản phẩm đã mua. Khi chưa đăng nhập (khách vãng lai), chỉ được xem sản phẩm, không thấy voucher và khi thêm vào giỏ hàng sẽ yêu cầu đăng nhập.

---

# CHƯƠNG 5: KẾT NỐI VÀ VẬN HÀNH ỨNG DỤNG (SPRING BOOT + SQL SERVER)

## 5.1. Cấu hình kết nối Driver
- **JDBC Driver:** `com.microsoft.sqlserver.jdbc.SQLServerDriver` (phiên bản `mssql-jdbc 13.4.0.jre11`).
- **Hibernate Dialect:** `org.hibernate.dialect.SQLServerDialect`.
- **Chuỗi kết nối chuẩn:**
  ```properties
  spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QL_NhaSach;encrypt=true;trustServerCertificate=true;
  spring.datasource.username=the4book_user
  spring.datasource.password=ChangeMe@2026
  ```

## 5.2. Kết quả kiểm thử thực nghiệm trên giao diện
1. **Trang chủ & Danh mục sản phẩm (`/`, `/san-pham`):** Tải danh sách sách từ CSDL SQL Server cục bộ, lọc theo danh mục, mức giá và hiển thị gian hàng cung cấp (HTTP 200 OK).
2. **Chi tiết sản phẩm (`/san-pham/{id}`):** Hiển thị chi tiết sách, tên nhà xuất bản dạng text độc lập, số lượng tồn kho theo thời gian thực và điểm đánh giá từ hàm UDF (HTTP 200 OK).
3. **Giỏ hàng & Đặt hàng (`/gio-hang`, `/gio-hang/dat-hang`):**
   - Thêm sản phẩm vào giỏ hàng: lưu bản ghi vào `chi_tiet_gio_hang` trong SQL Server.
   - Đặt hàng: tạo thành công đơn hàng số #5, tự động tính phí sàn 5%, trừ tồn kho sách từ 49 xuống 47, ghi nhận thanh toán `thanh_toan` (HTTP 200 OK).
4. **Kênh người bán Vendor (`/vendor/dashboard`, `/vendor/inventory`):** Gian hàng đăng nhập bằng tài khoản `vendor_kimdong`, truy cập bảng kiểm kê kho hàng của shop, xác nhận đơn hàng qua thủ tục lưu trữ `sp_ShopXacNhanDonHang` (HTTP 200 OK).
5. **Kênh Quản trị Admin (`/admin/dashboard`):** Đăng nhập bằng tài khoản `admin`, giám sát số liệu vận hành và doanh thu toàn sàn (HTTP 200 OK).

## 5.3. Phiên bản triển khai trực tuyến (Live Render Showcase)
Bên cạnh môi trường chạy cục bộ kết nối SQL Server 2022/2025 phục vụ chấm đồ án và bảo vệ chuyên sâu về Hệ quản trị CSDL, nhóm dự án đã triển khai song song phiên bản ứng dụng Web thực tế lên nền tảng đám mây:
- **Đường dẫn truy cập trực tiếp:** [https://the4bookstore-phor.onrender.com/](https://the4bookstore-phor.onrender.com/)
- **Mục đích:** Giúp Hội đồng giảng viên và người dùng bên ngoài có thể truy cập, trải nghiệm ngay toàn bộ giao diện sàn sách trực tuyến, khám phá các gian hàng và tính năng đặt hàng/săn voucher 24/7 mà không cần cài đặt môi trường máy chủ cục bộ.

---

# CHƯƠNG 6: HƯỚNG DẪN SAO LƯU & PHỤC HỒI CSDL (.BAK)

## 6.1. File Backup chuẩn
- **File:** `DOCUMENT/scripts/QL_NhaSach.bak`
- **Kích thước:** ~8.37 MB
- **Loại:** Full Database Backup chứa đầy đủ 22+ bảng chuẩn 3NF, 5 Trigger, 5 View, 5 Index, 5 Stored Procedure, 5 Functions và 4 Roles.

## 6.2. Lệnh phục hồi (Restore) trong SQL Server Management Studio (SSMS)
```sql
USE master;
GO

-- 1. Ngắt tất cả kết nối hiện tại đến CSDL QL_NhaSach
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QL_NhaSach')
BEGIN
    ALTER DATABASE QL_NhaSach SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
END
GO

-- 2. Phục hồi CSDL từ file backup .bak
RESTORE DATABASE QL_NhaSach
FROM DISK = N'C:\Users\Phat\the4BookStore\DOCUMENT\scripts\QL_NhaSach.bak'
WITH REPLACE, RECOVERY;
GO

-- 3. Mở lại chế độ đa người dùng
ALTER DATABASE QL_NhaSach SET MULTI_USER;
GO
```
