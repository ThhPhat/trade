-- =========================================================================
-- THE4BOOKSTORE MARKETPLACE MIGRATION SCRIPT
-- Nâng cấp hệ thống từ Single-Vendor sang Multi-Vendor Marketplace
-- =========================================================================

-- 1. BẢNG CẤU HÌNH HỆ THỐNG TOÀN SÀN
CREATE TABLE IF NOT EXISTS cau_hinh_he_thong (
    ma_cau_hinh VARCHAR(50) PRIMARY KEY,
    gia_tri VARCHAR(255) NOT NULL,
    mo_ta VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO cau_hinh_he_thong (ma_cau_hinh, gia_tri, mo_ta) VALUES 
('PHI_SAN_MAC_DINH', '5.0', 'Tỷ lệ phần trăm phí hoa hồng sàn mặc định (%)'),
('MIN_REVIEW_LENGTH', '50', 'Số ký tự tối thiểu của bài đánh giá')
ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri);

-- 2. BẢNG NHÀ VẬN CHUYỂN
CREATE TABLE IF NOT EXISTS nha_van_chuyen (
    ma_nvc INT AUTO_INCREMENT PRIMARY KEY,
    ten_nvc VARCHAR(100) NOT NULL,
    phi_co_ban INT NOT NULL DEFAULT 25000,
    thoi_gian_du_kien VARCHAR(50) NOT NULL DEFAULT '2 - 3 ngày',
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'HoatDong'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO nha_van_chuyen (ma_nvc, ten_nvc, phi_co_ban, thoi_gian_du_kien, trang_thai) VALUES
(1, 'Giao Hàng Nhanh (GHN)', 25000, '2 - 3 ngày', 'HoatDong'),
(2, 'Giao Hàng Tiết Kiệm (GHTK)', 22000, '3 - 4 ngày', 'HoatDong'),
(3, 'Viettel Post', 28000, '1 - 2 ngày', 'HoatDong'),
(4, 'Hỏa Tốc 2H (Nội thành)', 45000, '2 giờ', 'HoatDong')
ON DUPLICATE KEY UPDATE ten_nvc = VALUES(ten_nvc);

-- 3. BẢNG GIAN HÀNG (SHOP)
CREATE TABLE IF NOT EXISTS shop (
    ma_shop INT AUTO_INCREMENT PRIMARY KEY,
    ma_tai_khoan INT NOT NULL UNIQUE,
    ten_shop VARCHAR(150) NOT NULL UNIQUE,
    slug VARCHAR(150) NOT NULL UNIQUE,
    logo VARCHAR(500) NULL,
    banner VARCHAR(500) NULL,
    mo_ta TEXT NULL,
    dia_chi_shop VARCHAR(255) NULL,
    so_dien_thoai VARCHAR(20) NULL,
    email_shop VARCHAR(100) NULL,
    chiet_khau_phan_tram DECIMAL(5,2) NULL,
    trang_thai VARCHAR(30) NOT NULL DEFAULT 'ChoDuyet',
    ngay_tao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat DATETIME NULL,
    CONSTRAINT fk_shop_taikhoan FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan(ma_tai_khoan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo shop mặc định cho hệ thống (The4BookStore Official gắn với tài khoản admin id=4)
INSERT INTO shop (ma_shop, ma_tai_khoan, ten_shop, slug, logo, banner, mo_ta, dia_chi_shop, so_dien_thoai, email_shop, chiet_khau_phan_tram, trang_thai, ngay_tao)
VALUES (1, 4, 'The4BookStore Official', 'the4bookstore-official', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150', 'https://images.unsplash.com/photo-1507842229451-7f01be45c06b?w=1200', 'Gian hàng chính thức của hệ thống The4BookStore.', 'Số 1 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh', '0903943105', 'official@the4bookstore.vn', 5.00, 'HoatDong', NOW())
ON DUPLICATE KEY UPDATE ten_shop = VALUES(ten_shop);

-- 4. BẢNG SỔ ĐỊA CHỈ NHẬN HÀNG CỦA USER
CREATE TABLE IF NOT EXISTS dia_chi_giao_hang (
    ma_dia_chi INT AUTO_INCREMENT PRIMARY KEY,
    makh INT NOT NULL,
    ten_nguoi_nhan VARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(20) NOT NULL,
    dia_chi_chi_tiet VARCHAR(255) NOT NULL,
    phuong_xa VARCHAR(100) NULL,
    quan_huyen VARCHAR(100) NULL,
    tinh_thanh VARCHAR(100) NOT NULL,
    la_mac_dinh BIT NOT NULL DEFAULT 0,
    ngay_tao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dc_khachhang FOREIGN KEY (makh) REFERENCES khach_hang(makh) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG SẢN PHẨM YÊU THÍCH (WISHLIST)
CREATE TABLE IF NOT EXISTS san_pham_yeu_thich (
    makh INT NOT NULL,
    masp INT NOT NULL,
    ngay_thich DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (makh, masp),
    CONSTRAINT fk_yt_khachhang FOREIGN KEY (makh) REFERENCES khach_hang(makh) ON DELETE CASCADE,
    CONSTRAINT fk_yt_sanpham FOREIGN KEY (masp) REFERENCES san_pham(masp) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG SẢN PHẨM ĐÃ XEM (RECENTLY VIEWED)
CREATE TABLE IF NOT EXISTS san_pham_da_xem (
    makh INT NOT NULL,
    masp INT NOT NULL,
    thoi_gian_xem DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (makh, masp),
    CONSTRAINT fk_dx_khachhang FOREIGN KEY (makh) REFERENCES khach_hang(makh) ON DELETE CASCADE,
    CONSTRAINT fk_dx_sanpham FOREIGN KEY (masp) REFERENCES san_pham(masp) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. BẢNG MEDIA ĐÁNH GIÁ (ẢNH / VIDEO)
CREATE TABLE IF NOT EXISTS danh_gia_media (
    ma_media INT AUTO_INCREMENT PRIMARY KEY,
    ma_danh_gia INT NOT NULL,
    loai_media VARCHAR(10) NOT NULL DEFAULT 'IMAGE', -- IMAGE hoặc VIDEO
    url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_dgm_danhgia FOREIGN KEY (ma_danh_gia) REFERENCES danh_gia(ma_danh_gia) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. CẬP NHẬT CỘT CHO BẢNG SAN_PHAM
SET @dbname = DATABASE();
SET @tablename = "san_pham";

-- Thêm ma_shop
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "ma_shop") > 0,
  "SELECT 1",
  "ALTER TABLE san_pham ADD COLUMN ma_shop INT NULL, ADD CONSTRAINT fk_sp_shop FOREIGN KEY (ma_shop) REFERENCES shop(ma_shop);"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm trang_thai_khoa
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "trang_thai_khoa") > 0,
  "SELECT 1",
  "ALTER TABLE san_pham ADD COLUMN trang_thai_khoa VARCHAR(30) NOT NULL DEFAULT 'BinhThuong';"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm so_luong_da_ban
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "so_luong_da_ban") > 0,
  "SELECT 1",
  "ALTER TABLE san_pham ADD COLUMN so_luong_da_ban INT NOT NULL DEFAULT 0;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm hinh_anh
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "hinh_anh") > 0,
  "SELECT 1",
  "ALTER TABLE san_pham ADD COLUMN hinh_anh VARCHAR(500) NULL;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Gán ma_shop mặc định là 1 cho các sách hiện có
UPDATE san_pham SET ma_shop = 1 WHERE ma_shop IS NULL;
UPDATE san_pham SET so_luong_da_ban = 35 WHERE masp = 1;
UPDATE san_pham SET so_luong_da_ban = 24 WHERE masp = 3;
UPDATE san_pham SET so_luong_da_ban = 18 WHERE masp = 6;
UPDATE san_pham SET so_luong_da_ban = 12 WHERE masp = 7;
UPDATE san_pham SET so_luong_da_ban = 11 WHERE masp = 2;

-- 9. CẬP NHẬT CỘT CHO BẢNG DON_HANG
SET @tablename = "don_hang";

-- Thêm ma_shop
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "ma_shop") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN ma_shop INT NULL, ADD CONSTRAINT fk_dh_shop FOREIGN KEY (ma_shop) REFERENCES shop(ma_shop);"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm ma_nvc
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "ma_nvc") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN ma_nvc INT NULL, ADD CONSTRAINT fk_dh_nvc FOREIGN KEY (ma_nvc) REFERENCES nha_van_chuyen(ma_nvc);"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm phi_van_chuyen
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "phi_van_chuyen") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN phi_van_chuyen INT NOT NULL DEFAULT 0;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm chiet_khau_app_phan_tram
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "chiet_khau_app_phan_tram") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN chiet_khau_app_phan_tram DECIMAL(5,2) NOT NULL DEFAULT 5.00;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm tien_phi_san
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "tien_phi_san") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN tien_phi_san INT NOT NULL DEFAULT 0;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm tien_thuc_nhan_shop
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "tien_thuc_nhan_shop") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN tien_thuc_nhan_shop INT NOT NULL DEFAULT 0;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm phuong_thuc_thanh_toan
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "phuong_thuc_thanh_toan") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN phuong_thuc_thanh_toan VARCHAR(30) NOT NULL DEFAULT 'COD';"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm trang_thai_thanh_toan
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "trang_thai_thanh_toan") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN trang_thai_thanh_toan VARCHAR(30) NOT NULL DEFAULT 'ChuaThanhToan';"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm ly_do_tra_hang
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "ly_do_tra_hang") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN ly_do_tra_hang TEXT NULL;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm phan_quyet_tranh_chap
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "phan_quyet_tranh_chap") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN phan_quyet_tranh_chap TEXT NULL;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm nguoi_xu_ly_tranh_chap
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "nguoi_xu_ly_tranh_chap") > 0,
  "SELECT 1",
  "ALTER TABLE don_hang ADD COLUMN nguoi_xu_ly_tranh_chap INT NULL, ADD CONSTRAINT fk_dh_manager FOREIGN KEY (nguoi_xu_ly_tranh_chap) REFERENCES tai_khoan(ma_tai_khoan);"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Gán ma_shop = 1 cho các đơn hàng cũ
UPDATE don_hang SET ma_shop = 1, ma_nvc = 1 WHERE ma_shop IS NULL;
UPDATE don_hang SET tien_phi_san = ROUND(tong_tien * 0.05), tien_thuc_nhan_shop = tong_tien - ROUND(tong_tien * 0.05) WHERE tien_thuc_nhan_shop = 0;

-- 10. CẬP NHẬT CỘT CHO BẢNG KHUYEN_MAI
SET @tablename = "khuyen_mai";

-- Thêm loai_khuyen_mai
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "loai_khuyen_mai") > 0,
  "SELECT 1",
  "ALTER TABLE khuyen_mai ADD COLUMN loai_khuyen_mai VARCHAR(30) NOT NULL DEFAULT 'GIAM_GIA_SAN_PHAM';"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm pham_vi
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "pham_vi") > 0,
  "SELECT 1",
  "ALTER TABLE khuyen_mai ADD COLUMN pham_vi VARCHAR(20) NOT NULL DEFAULT 'TOAN_SAN';"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm ma_shop
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = "ma_shop") > 0,
  "SELECT 1",
  "ALTER TABLE khuyen_mai ADD COLUMN ma_shop INT NULL, ADD CONSTRAINT fk_km_shop FOREIGN KEY (ma_shop) REFERENCES shop(ma_shop);"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 11. CẬP NHẬT TÀI KHOẢN VÍ DỤ VENDOR & MANAGER
-- Thêm tài khoản vendor mẫu nếu chưa có
INSERT INTO tai_khoan (ten_dang_nhap, email, mat_khau_hash, vai_tro, trang_thai, auth_provider, ngay_tao)
SELECT 'vendor_demo', 'vendor@the4bookstore.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'VENDOR', 'HoatDong', 'LOCAL', NOW()
WHERE NOT EXISTS (SELECT 1 FROM tai_khoan WHERE ten_dang_nhap = 'vendor_demo');

-- Thêm shop mẫu cho vendor_demo
INSERT INTO shop (ma_tai_khoan, ten_shop, slug, logo, banner, mo_ta, dia_chi_shop, so_dien_thoai, email_shop, chiet_khau_phan_tram, trang_thai, ngay_tao)
SELECT t.ma_tai_khoan, 'Nhà Sách Trí Tuệ', 'nha-sach-tri-tue', 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=150', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=1200', 'Chuyên cung cấp sách kỹ năng sống và sách kinh tế chọn lọc.', '123 Cầu Giấy, Hà Nội', '0912345678', 'trituebook@gmail.com', 5.00, 'HoatDong', NOW()
FROM tai_khoan t WHERE t.ten_dang_nhap = 'vendor_demo'
AND NOT EXISTS (SELECT 1 FROM shop WHERE ten_shop = 'Nhà Sách Trí Tuệ');

