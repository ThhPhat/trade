-- ====================================================================
-- THE4BOOKSTORE - MIGRATION SCRIPT FOR VENDOR WAREHOUSE & STOCK REQUESTS
-- Cập nhật cấu trúc bảng cho tính năng Quản lý kho Vendor & Cấp hàng
-- ====================================================================

USE QL_NhaSach;

-- 1. Cập nhật bảng phieu_nhap
ALTER TABLE phieu_nhap 
  MODIFY COLUMN mancc int NULL,
  MODIFY COLUMN manv int NULL,
  MODIFY COLUMN ma_kho int NULL,
  ADD COLUMN nha_cung_cap_ten varchar(255) NULL,
  ADD COLUMN ma_shop int NULL;

ALTER TABLE phieu_nhap 
  ADD CONSTRAINT FK_pn_shop FOREIGN KEY (ma_shop) REFERENCES shop (ma_shop);

-- 2. Cập nhật bảng phieu_kiem_ke
ALTER TABLE phieu_kiem_ke 
  MODIFY COLUMN manv int NULL,
  MODIFY COLUMN ma_kho int NULL,
  ADD COLUMN ma_shop int NULL;

ALTER TABLE phieu_kiem_ke 
  ADD CONSTRAINT FK_pkk_shop FOREIGN KEY (ma_shop) REFERENCES shop (ma_shop);

-- 3. Tạo bảng yêu cầu cấp hàng (yeu_cau_cap_hang)
CREATE TABLE IF NOT EXISTS yeu_cau_cap_hang (
  ma_yeu_cau int NOT NULL AUTO_INCREMENT,
  ma_shop int NOT NULL,
  ma_kho int NOT NULL,
  ma_nhan_vien_duyet int DEFAULT NULL,
  trang_thai varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ChoDuyet',
  ngay_tao datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ngay_duyet datetime DEFAULT NULL,
  ghi_chu varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ly_do_tu_choi varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (ma_yeu_cau),
  KEY fk_ycch_shop (ma_shop),
  KEY fk_ycch_kho (ma_kho),
  KEY fk_ycch_nv (ma_nhan_vien_duyet),
  CONSTRAINT fk_ycch_shop FOREIGN KEY (ma_shop) REFERENCES shop (ma_shop) ON DELETE CASCADE,
  CONSTRAINT fk_ycch_kho FOREIGN KEY (ma_kho) REFERENCES kho (ma_kho),
  CONSTRAINT fk_ycch_nv FOREIGN KEY (ma_nhan_vien_duyet) REFERENCES nhan_vien (manv) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tạo bảng chi tiết cấp hàng (chi_tiet_cap_hang)
CREATE TABLE IF NOT EXISTS chi_tiet_cap_hang (
  ma_chi_tiet int NOT NULL AUTO_INCREMENT,
  ma_yeu_cau int NOT NULL,
  masp int NOT NULL,
  so_luong_yeu_cau int NOT NULL,
  so_luong_duyet int DEFAULT NULL,
  PRIMARY KEY (ma_chi_tiet),
  KEY fk_ctch_yeucau (ma_yeu_cau),
  KEY fk_ctch_sanpham (masp),
  CONSTRAINT fk_ctch_yeucau FOREIGN KEY (ma_yeu_cau) REFERENCES yeu_cau_cap_hang (ma_yeu_cau) ON DELETE CASCADE,
  CONSTRAINT fk_ctch_sanpham FOREIGN KEY (masp) REFERENCES san_pham (masp) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
