START TRANSACTION;

DELETE FROM password_reset_token WHERE ma_tai_khoan NOT IN (1, 4, 5, 16, 17, 22);
DELETE FROM khach_hang WHERE ma_tai_khoan NOT IN (1, 4, 5, 16, 17, 22);
DELETE FROM tai_khoan WHERE ma_tai_khoan NOT IN (1, 4, 5, 16, 17, 22);

-- Cập nhật mật khẩu 123456 cho tất cả các tài khoản
UPDATE tai_khoan 
SET mat_khau_hash = '$2a$10$4TK5WyWlJpxNW8X6W6ZuN.iY2lQ6kj3lhCp/pAbjA03lbIoZozy7m',
    trang_thai = 'HoatDong'
WHERE ma_tai_khoan IN (1, 4, 5, 16, 17, 22);

UPDATE tai_khoan SET ten_dang_nhap = 'admin', vai_tro = 'ADMIN', email = 'admin@the4bookstore.vn' WHERE ma_tai_khoan = 4;
UPDATE tai_khoan SET ten_dang_nhap = 'manager', vai_tro = 'MANAGER', email = 'manager@the4bookstore.vn' WHERE ma_tai_khoan = 5;
UPDATE tai_khoan SET ten_dang_nhap = 'user', vai_tro = 'USER', email = 'user@the4bookstore.vn' WHERE ma_tai_khoan = 17;
UPDATE tai_khoan SET ten_dang_nhap = 'vendor_demo', vai_tro = 'VENDOR', email = 'vendor@the4bookstore.vn' WHERE ma_tai_khoan = 22;
UPDATE tai_khoan SET ten_dang_nhap = 'vendor_fahasa', vai_tro = 'VENDOR', email = 'fahasa@the4bookstore.vn' WHERE ma_tai_khoan = 16;
UPDATE tai_khoan SET ten_dang_nhap = 'vendor_hoasen', vai_tro = 'VENDOR', email = 'hoasen@the4bookstore.vn' WHERE ma_tai_khoan = 1;

COMMIT;
