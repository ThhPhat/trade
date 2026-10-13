-- ====================================================================
-- THE4BOOKSTORE - AUDIT LOG & TRIGGERS
-- Ghi nhật ký thay đổi dữ liệu quan trọng
-- ====================================================================

USE QL_NhaSach;

-- Bảng nhật ký bảo mật
CREATE TABLE IF NOT EXISTS nhat_ky_bao_mat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bang_ten VARCHAR(64) NOT NULL COMMENT 'Tên bảng bị thay đổi',
    hanh_dong ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    ban_ghi_id INT COMMENT 'ID bản ghi bị ảnh hưởng',
    du_lieu_cu JSON COMMENT 'Dữ liệu trước khi thay đổi',
    du_lieu_moi JSON COMMENT 'Dữ liệu sau khi thay đổi', 
    nguoi_thuc_hien VARCHAR(100) DEFAULT (CURRENT_USER()),
    thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nkbm_bang (bang_ten),
    INDEX idx_nkbm_thoi_gian (thoi_gian),
    INDEX idx_nkbm_hanh_dong (hanh_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Trigger: Ghi log khi thay đổi vai trò hoặc trạng thái tài khoản
DELIMITER //

CREATE TRIGGER IF NOT EXISTS trg_tai_khoan_update 
AFTER UPDATE ON tai_khoan
FOR EACH ROW
BEGIN
    IF OLD.vai_tro != NEW.vai_tro OR OLD.trang_thai != NEW.trang_thai OR OLD.mat_khau_hash != NEW.mat_khau_hash THEN
        INSERT INTO nhat_ky_bao_mat (bang_ten, hanh_dong, ban_ghi_id, du_lieu_cu, du_lieu_moi)
        VALUES (
            'tai_khoan', 'UPDATE', NEW.ma_tai_khoan,
            JSON_OBJECT('vai_tro', OLD.vai_tro, 'trang_thai', OLD.trang_thai, 'ten_dang_nhap', OLD.ten_dang_nhap),
            JSON_OBJECT('vai_tro', NEW.vai_tro, 'trang_thai', NEW.trang_thai, 'ten_dang_nhap', NEW.ten_dang_nhap)
        );
    END IF;
END //

-- Trigger: Ghi log khi thay đổi trạng thái đơn hàng
CREATE TRIGGER IF NOT EXISTS trg_don_hang_update
AFTER UPDATE ON don_hang
FOR EACH ROW
BEGIN
    IF OLD.trang_thai != NEW.trang_thai THEN
        INSERT INTO nhat_ky_bao_mat (bang_ten, hanh_dong, ban_ghi_id, du_lieu_cu, du_lieu_moi)
        VALUES (
            'don_hang', 'UPDATE', NEW.madh,
            JSON_OBJECT('trang_thai', OLD.trang_thai, 'tong_tien', OLD.tong_tien),
            JSON_OBJECT('trang_thai', NEW.trang_thai, 'tong_tien', NEW.tong_tien)
        );
    END IF;
END //

-- Trigger: Ghi log khi xóa sản phẩm
CREATE TRIGGER IF NOT EXISTS trg_san_pham_delete
BEFORE DELETE ON san_pham
FOR EACH ROW
BEGIN
    INSERT INTO nhat_ky_bao_mat (bang_ten, hanh_dong, ban_ghi_id, du_lieu_cu)
    VALUES (
        'san_pham', 'DELETE', OLD.masp,
        JSON_OBJECT('tensp', OLD.tensp, 'gia_ban', OLD.gia_ban, 'isbn', OLD.isbn)
    );
END //

-- Trigger: Ghi log khi thay đổi giá sản phẩm
CREATE TRIGGER IF NOT EXISTS trg_san_pham_update
AFTER UPDATE ON san_pham
FOR EACH ROW
BEGIN
    IF OLD.gia_ban != NEW.gia_ban OR OLD.trang_thai != NEW.trang_thai THEN
        INSERT INTO nhat_ky_bao_mat (bang_ten, hanh_dong, ban_ghi_id, du_lieu_cu, du_lieu_moi)
        VALUES (
            'san_pham', 'UPDATE', NEW.masp,
            JSON_OBJECT('tensp', OLD.tensp, 'gia_ban', OLD.gia_ban, 'trang_thai', OLD.trang_thai),
            JSON_OBJECT('tensp', NEW.tensp, 'gia_ban', NEW.gia_ban, 'trang_thai', NEW.trang_thai)
        );
    END IF;
END //

DELIMITER ;

-- Stored procedure: Dọn dẹp log cũ (giữ lại 90 ngày)
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_DonDepNhatKyBaoMat(IN p_so_ngay_giu INT)
BEGIN
    DELETE FROM nhat_ky_bao_mat WHERE thoi_gian < DATE_SUB(NOW(), INTERVAL p_so_ngay_giu DAY);
    SELECT ROW_COUNT() AS so_ban_ghi_da_xoa;
END //
DELIMITER ;
