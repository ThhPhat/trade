-- ====================================================================
-- THE4BOOKSTORE - THIẾT LẬP BẢO MẬT & PHÂN QUYỀN CƠ SỞ DỮ LIỆU
-- Chuẩn hóa theo giáo trình: "Chương 6: Bảo mật và phân quyền"
-- Giảng viên hướng dẫn: TS. Phan Thị Thể - FIT - HCM-UTE
-- Hệ quản trị cơ sở dữ liệu: MySQL 8.0+
-- ====================================================================

USE QL_NhaSach;

-- ====================================================================
-- PHẦN 1: TÀI KHOẢN HỆ THỐNG THEO NGUYÊN TẮC ĐẶC QUYỀN TỐI THIỂU (LEAST PRIVILEGE)
-- (Tham khảo Slide 7, 10, 18: Tách biệt Login/User theo chức năng vận hành)
-- ====================================================================

-- 1.1. Tài khoản dành cho Ứng dụng Web Spring Boot (bookstore_app)
-- Chỉ cấp quyền DML (SELECT, INSERT, UPDATE, DELETE) và EXECUTE thủ tục/hàm.
-- TUYỆT ĐỐI KHÔNG cấp quyền DDL (DROP, ALTER, CREATE TABLE) để chống leo thang đặc quyền khi bị tấn công.
CREATE USER IF NOT EXISTS 'bookstore_app'@'%' IDENTIFIED BY 'ChangeMe@App2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON QL_NhaSach.* TO 'bookstore_app'@'%';
GRANT EXECUTE ON QL_NhaSach.* TO 'bookstore_app'@'%';

-- 1.2. Tài khoản dành cho Sao lưu & Phục hồi dữ liệu (bookstore_backup)
-- Chỉ cấp quyền đọc và khóa bảng phục vụ mysqldump, không được sửa dữ liệu.
-- Lưu ý: PROCESS là Global Privilege (ở mức *.*) theo quy định MySQL, không thể gán trên DB level.
CREATE USER IF NOT EXISTS 'bookstore_backup'@'localhost' IDENTIFIED BY 'ChangeMe@Backup2026';
GRANT SELECT, LOCK TABLES, SHOW VIEW, EVENT, TRIGGER ON QL_NhaSach.* TO 'bookstore_backup'@'localhost';
GRANT PROCESS ON *.* TO 'bookstore_backup'@'localhost';

-- 1.3. Tài khoản Quản trị CSDL - DBA (bookstore_dba)
-- Toàn quyền trên CSDL QL_NhaSach, chỉ dùng cho kỹ sư DBA chạy migration hoặc bảo trì.
CREATE USER IF NOT EXISTS 'bookstore_dba'@'localhost' IDENTIFIED BY 'ChangeMe@DBA2026';
GRANT ALL PRIVILEGES ON QL_NhaSach.* TO 'bookstore_dba'@'localhost';


-- ====================================================================
-- PHẦN 2: BẢO MẬT BẰNG VIEW (VIEW-BASED SECURITY)
-- (Tham khảo Slide 26 - 31: Giới hạn dòng Horizontal, giới hạn cột Vertical, WITH CHECK OPTION)
-- ====================================================================

-- 2.1. View ẩn thông tin nhạy cảm tài khoản (Vertical Restriction - Slide 27, 28)
-- Giới hạn theo chiều dọc: Che giấu mat_khau_hash, token_version, otp
CREATE OR REPLACE VIEW vw_tai_khoan_cong_khai AS
SELECT 
    ma_tai_khoan, 
    ten_dang_nhap, 
    email, 
    vai_tro, 
    trang_thai, 
    auth_provider, 
    ngay_tao
FROM tai_khoan;

-- 2.2. View che giấu thông tin khách hàng cho nhân viên thông thường (Vertical Restriction)
-- Nhân viên chỉ thấy họ tên, mã tài khoản, ẩn địa chỉ và số điện thoại chi tiết
CREATE OR REPLACE VIEW vw_khach_hang_an_thong_tin AS
SELECT 
    makh, 
    ho_ten, 
    email, 
    CONCAT(SUBSTRING(so_dien_thoai, 1, 3), '****', SUBSTRING(so_dien_thoai, -3)) AS so_dien_thoai_che,
    ngay_dang_ky
FROM khach_hang;

-- 2.3. View sản phẩm đang kinh doanh với WITH CHECK OPTION (Horizontal Restriction - Slide 28, 30)
-- Giới hạn theo chiều ngang: Chỉ xem sản phẩm 'HoatDong'.
-- WITH CHECK OPTION: Chặn nhân viên cố tình INSERT hoặc UPDATE sản phẩm về trạng thái khác qua View này.
CREATE OR REPLACE VIEW vw_san_pham_dang_ban AS
SELECT 
    masp, 
    isbn, 
    tensp, 
    gia_ban, 
    so_luong_ton, 
    loaisp, 
    trang_thai, 
    ma_danh_muc, 
    ma_shop
FROM san_pham
WHERE trang_thai = 'HoatDong'
WITH CHECK OPTION;

-- 2.4. View đơn hàng đã hoàn tất để nhân viên kế toán / thống kê tra cứu (Horizontal Restriction)
CREATE OR REPLACE VIEW vw_don_hang_hoan_tat AS
SELECT 
    madh, 
    ngay_dat, 
    ngay_hoan_thanh, 
    tong_tien, 
    tien_giam, 
    phi_van_chuyen, 
    tien_phi_san, 
    tien_thuc_nhan_shop, 
    phuong_thuc_thanh_toan, 
    ma_shop
FROM don_hang
WHERE trang_thai IN ('DaGiao', 'HoanTat');


-- ====================================================================
-- PHẦN 3: PHÂN QUYỀN DỰA TRÊN ROLE (ROLE-BASED AUTHORIZATION - RBAC)
-- (Tham khảo Slide 17, 32 - 41: Tạo Role nghiệp vụ, gán quyền, gán User vào Role)
-- ====================================================================

-- 3.1. Khởi tạo các Role theo đúng chức năng nghiệp vụ của nhà sách (Slide 36)
CREATE ROLE IF NOT EXISTS 'role_nhanvien_kho';
CREATE ROLE IF NOT EXISTS 'role_nhanvien_banhang';
CREATE ROLE IF NOT EXISTS 'role_quanly';
CREATE ROLE IF NOT EXISTS 'role_gianhang';

-- 3.2. Phân quyền cho Role Quản lý kho (role_nhanvien_kho - Slide 37)
-- Được toàn quyền DML trên kho hàng, phiếu nhập, phiếu kiểm kê; chỉ được đọc thông tin sản phẩm
GRANT SELECT, INSERT, UPDATE ON QL_NhaSach.kho_hang TO 'role_nhanvien_kho';
GRANT SELECT, INSERT, UPDATE, DELETE ON QL_NhaSach.phieu_nhap TO 'role_nhanvien_kho';
GRANT SELECT, INSERT, UPDATE, DELETE ON QL_NhaSach.chi_tiet_phieu_nhap TO 'role_nhanvien_kho';
GRANT SELECT, INSERT, UPDATE, DELETE ON QL_NhaSach.phieu_kiem_ke TO 'role_nhanvien_kho';
GRANT SELECT, INSERT, UPDATE, DELETE ON QL_NhaSach.chi_tiet_kiem_ke TO 'role_nhanvien_kho';
GRANT SELECT ON QL_NhaSach.san_pham TO 'role_nhanvien_kho';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_XacNhanPhieuNhap TO 'role_nhanvien_kho';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_XacNhanPhieuKiemKe TO 'role_nhanvien_kho';

-- 3.3. Phân quyền cho Role Nhân viên bán hàng (role_nhanvien_banhang - Slide 37)
-- Được xử lý đơn hàng, xem khách hàng đã che thông tin, xem sản phẩm đang bán qua VIEW (Slide 29: Không cấp trực tiếp trên bảng gốc)
GRANT SELECT, UPDATE ON QL_NhaSach.don_hang TO 'role_nhanvien_banhang';
GRANT SELECT ON QL_NhaSach.chi_tiet_don_hang TO 'role_nhanvien_banhang';
GRANT SELECT ON QL_NhaSach.vw_khach_hang_an_thong_tin TO 'role_nhanvien_banhang';
GRANT SELECT ON QL_NhaSach.vw_san_pham_dang_ban TO 'role_nhanvien_banhang';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_CapNhatTrangThaiDonHang TO 'role_nhanvien_banhang';

-- 3.4. Phân quyền cho Role Gian hàng / Vendor (role_gianhang)
-- Quản lý sản phẩm của gian hàng và xử lý đơn hàng qua các Stored Procedure đối soát
GRANT SELECT, INSERT, UPDATE ON QL_NhaSach.san_pham TO 'role_gianhang';
GRANT SELECT ON QL_NhaSach.don_hang TO 'role_gianhang';
GRANT SELECT ON QL_NhaSach.chi_tiet_don_hang TO 'role_gianhang';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_VendorXacNhanDonHang TO 'role_gianhang';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_VendorHuyDonHang TO 'role_gianhang';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_VendorXuLyHoanHang TO 'role_gianhang';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_DoanhThuShopTheoThang TO 'role_gianhang';

-- 3.5. Phân quyền cho Role Quản lý (role_quanly)
-- Xem báo cáo, doanh thu qua View, duyệt tranh chấp, quản lý khuyến mãi
GRANT SELECT ON QL_NhaSach.vw_don_hang_hoan_tat TO 'role_quanly';
GRANT SELECT ON QL_NhaSach.vw_tai_khoan_cong_khai TO 'role_quanly';
GRANT SELECT, INSERT, UPDATE, DELETE ON QL_NhaSach.khuyen_mai TO 'role_quanly';
GRANT SELECT, UPDATE ON QL_NhaSach.shop TO 'role_quanly';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_DoanhThuTheoThang TO 'role_quanly';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_KhoaShopVaSanPham TO 'role_quanly';
GRANT EXECUTE ON PROCEDURE QL_NhaSach.sp_TopSachBanChay TO 'role_quanly';


-- ====================================================================
-- PHẦN 4: TẠO USER NHÂN VIÊN MẪU & GÁN VÀO ROLE (Slide 38)
-- ====================================================================

-- 4.1. Tạo User cho nhân viên kho và gán vào role_nhanvien_kho
CREATE USER IF NOT EXISTS 'nv_kho_01'@'localhost' IDENTIFIED BY 'Kho@123456';
GRANT 'role_nhanvien_kho' TO 'nv_kho_01'@'localhost';
SET DEFAULT ROLE ALL TO 'nv_kho_01'@'localhost';

-- 4.2. Tạo User cho nhân viên bán hàng và gán vào role_nhanvien_banhang
CREATE USER IF NOT EXISTS 'nv_banhang_01'@'localhost' IDENTIFIED BY 'BanHang@123456';
GRANT 'role_nhanvien_banhang' TO 'nv_banhang_01'@'localhost';
SET DEFAULT ROLE ALL TO 'nv_banhang_01'@'localhost';

-- 4.3. Tạo User cho quản lý và gán vào role_quanly
CREATE USER IF NOT EXISTS 'quanly_01'@'localhost' IDENTIFIED BY 'QuanLy@123456';
GRANT 'role_quanly' TO 'quanly_01'@'localhost';
SET DEFAULT ROLE ALL TO 'quanly_01'@'localhost';


-- ====================================================================
-- PHẦN 5: MINH HỌA GỠ BỎ QUYỀN (REVOKE) & KIỂM TRA QUYỀN (Slide 22, 24, 25, 39)
-- ====================================================================

-- Ví dụ khi nhân viên nghỉ việc hoặc chuyển bộ phận: Gỡ bỏ role khỏi user (Slide 38)
-- REVOKE 'role_nhanvien_kho' FROM 'nv_kho_01'@'localhost';

-- Xem các quyền được cấp cho một User/Role (Tương tự sys.database_permissions trên Slide 25):
-- SHOW GRANTS FOR 'nv_kho_01'@'localhost';
-- SHOW GRANTS FOR 'role_nhanvien_banhang';
-- SHOW GRANTS FOR 'bookstore_app'@'%';

FLUSH PRIVILEGES;

-- ====================================================================
-- HƯỚNG DẪN KIỂM TRA & SỬ DỤNG:
-- 1. Chạy toàn bộ file này bằng tài khoản root:
--    mysql -u root -p QL_NhaSach < DOCUMENT/scripts/db_security_setup.sql
-- 2. Chạy tiếp file audit triggers:
--    mysql -u root -p QL_NhaSach < DOCUMENT/scripts/db_audit_triggers.sql
-- 3. Cập nhật file .env của Spring Boot để dùng user bookstore_app:
--    DB_USERNAME=bookstore_app
--    DB_PASSWORD=ChangeMe@App2026
-- ====================================================================
