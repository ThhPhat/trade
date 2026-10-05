-- =========================================================================
-- THE4BOOKSTORE - MARKETPLACE STORED PROCEDURES & FUNCTIONS
-- Hệ quản trị cơ sở dữ liệu: MySQL 8.0
-- Dự án: The4BookStore Multi-Vendor Marketplace
-- =========================================================================

USE ql_nhasach;

-- =========================================================================
-- PHẦN 1: FUNCTIONS (HÀM TÍNH TOÁN TRẢ VỀ GIÁ TRỊ)
-- =========================================================================

-- 1. fn_TinhPhiSan: Tính phí hoa hồng sàn mà hệ thống thu từ gian hàng
DROP FUNCTION IF EXISTS fn_TinhPhiSan;
DELIMITER $$
CREATE FUNCTION fn_TinhPhiSan(p_tong_tien INT, p_ma_shop INT) 
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_chiet_khau DECIMAL(5,2) DEFAULT 5.00;
    
    -- Lấy tỷ lệ chiết khấu riêng của shop nếu có, mặc định là 5%
    SELECT COALESCE(chiet_khau_phan_tram, 5.00) INTO v_chiet_khau
    FROM shop 
    WHERE ma_shop = p_ma_shop;
    
    IF v_chiet_khau IS NULL THEN
        SET v_chiet_khau = 5.00;
    END IF;
    
    RETURN ROUND(p_tong_tien * (v_chiet_khau / 100.0));
END$$
DELIMITER ;

-- 2. fn_DemSachCuaShop: Đếm số lượng đầu sách đang hoạt động của một gian hàng
DROP FUNCTION IF EXISTS fn_DemSachCuaShop;
DELIMITER $$
CREATE FUNCTION fn_DemSachCuaShop(p_ma_shop INT) 
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_count
    FROM san_pham
    WHERE ma_shop = p_ma_shop
      AND (trang_thai_khoa IS NULL OR trang_thai_khoa != 'DaKhoa');
      
    RETURN v_count;
END$$
DELIMITER ;

-- 3. fn_TinhDiemDanhGiaSanPham: Tính điểm số sao trung bình của sản phẩm (thang điểm 5.0)
DROP FUNCTION IF EXISTS fn_TinhDiemDanhGiaSanPham;
DELIMITER $$
CREATE FUNCTION fn_TinhDiemDanhGiaSanPham(p_masp INT) 
RETURNS DECIMAL(2,1)
READS SQL DATA
BEGIN
    DECLARE v_diem DECIMAL(2,1) DEFAULT 5.0;
    
    SELECT COALESCE(ROUND(AVG(so_sao), 1), 5.0) INTO v_diem
    FROM danh_gia
    WHERE masp = p_masp;
    
    RETURN v_diem;
END$$
DELIMITER ;


-- =========================================================================
-- PHẦN 2: STORED PROCEDURES (THỦ TỤC LƯU TRỮ VỚI TRANSACTION & ACID)
-- =========================================================================

-- 4. sp_VendorXacNhanDonHang: Shop xác nhận đơn hàng, kiểm tra & trừ tồn kho, tăng số lượng đã bán
DROP PROCEDURE IF EXISTS sp_VendorXacNhanDonHang;
DELIMITER $$
CREATE PROCEDURE sp_VendorXacNhanDonHang(
    IN p_madh INT,
    IN p_ma_shop INT,
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_trang_thai VARCHAR(50);
    DECLARE done INT DEFAULT 0;
    DECLARE v_masp INT;
    DECLARE v_so_luong INT;
    DECLARE v_ton_kho INT;
    
    -- Con trỏ duyệt qua các sản phẩm trong đơn hàng
    DECLARE cur_ctdh CURSOR FOR 
        SELECT masp, so_luong FROM chi_tiet_don_hang WHERE madh = p_madh;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    -- Khối xử lý ngoại lệ giao dịch
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Lỗi hệ thống CSDL trong quá trình xử lý giao dịch';
    END;

    START TRANSACTION;
    
    -- Kiểm tra đơn hàng có tồn tại và thuộc shop này không
    SELECT COUNT(*), trang_thai INTO v_count, v_trang_thai
    FROM don_hang 
    WHERE madh = p_madh AND (ma_shop = p_ma_shop OR p_ma_shop IS NULL)
    GROUP BY trang_thai
    LIMIT 1;

    IF v_count = 0 THEN
        SET p_status = 'NOT_FOUND';
        SET p_message = 'Đơn hàng không tồn tại hoặc không thuộc quyền quản lý của gian hàng';
        ROLLBACK;
    ELSEIF v_trang_thai != 'ChoXacNhan' AND v_trang_thai != 'CHO_XAC_NHAN' AND v_trang_thai != 'DonHangMoi' THEN
        SET p_status = 'INVALID_STATE';
        SET p_message = CONCAT('Đơn hàng đang ở trạng thái [', v_trang_thai, '], không thể xác nhận lại');
        ROLLBACK;
    ELSE
        -- 1. Kiểm tra tồn kho của tất cả sản phẩm trong đơn
        SET done = 0;
        OPEN cur_ctdh;
        check_loop: LOOP
            FETCH cur_ctdh INTO v_masp, v_so_luong;
            IF done = 1 THEN
                LEAVE check_loop;
            END IF;
            
            SELECT so_luong_ton INTO v_ton_kho FROM san_pham WHERE masp = v_masp FOR UPDATE;
            IF v_ton_kho < v_so_luong THEN
                SET p_status = 'OUT_OF_STOCK';
                SET p_message = CONCAT('Sản phẩm mã #', v_masp, ' không đủ tồn kho (hiện còn ', v_ton_kho, ')');
                LEAVE check_loop;
            END IF;
        END LOOP;
        CLOSE cur_ctdh;
        
        -- 2. Nếu đủ hàng, tiến hành trừ tồn kho và cập nhật trạng thái đơn
        IF p_status IS NULL OR p_status != 'OUT_OF_STOCK' THEN
            SET done = 0;
            OPEN cur_ctdh;
            update_loop: LOOP
                FETCH cur_ctdh INTO v_masp, v_so_luong;
                IF done = 1 THEN
                    LEAVE update_loop;
                END IF;
                
                UPDATE san_pham 
                SET so_luong_ton = so_luong_ton - v_so_luong,
                    so_luong_da_ban = so_luong_da_ban + v_so_luong
                WHERE masp = v_masp;
            END LOOP;
            CLOSE cur_ctdh;
            
            -- Chuyển trạng thái đơn hàng sang DaXacNhan
            UPDATE don_hang 
            SET trang_thai = 'DaXacNhan' 
            WHERE madh = p_madh;
            
            SET p_status = 'SUCCESS';
            SET p_message = 'Đã xác nhận đơn hàng thành công và cập nhật tồn kho';
            COMMIT;
        ELSE
            ROLLBACK;
        END IF;
    END IF;
END$$
DELIMITER ;

-- 5. sp_KhoaShopVaSanPham: Admin khóa gian hàng và tự động khóa tất cả sách của gian hàng đó
DROP PROCEDURE IF EXISTS sp_KhoaShopVaSanPham;
DELIMITER $$
CREATE PROCEDURE sp_KhoaShopVaSanPham(
    IN p_ma_shop INT,
    IN p_ly_do TEXT,
    OUT p_so_luong_khoa INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Cập nhật trạng thái gian hàng thành BiKhoa
    UPDATE shop 
    SET trang_thai = 'BiKhoa', 
        ngay_cap_nhat = NOW() 
    WHERE ma_shop = p_ma_shop;
    
    -- Tự động khóa toàn bộ sản phẩm của gian hàng
    UPDATE san_pham 
    SET trang_thai_khoa = 'DaKhoa'
    WHERE ma_shop = p_ma_shop;
    
    SELECT ROW_COUNT() INTO p_so_luong_khoa;
    
    COMMIT;
END$$
DELIMITER ;

-- 6. sp_DoanhThuShopTheoThang: Báo cáo thống kê doanh thu, phí sàn và thực nhận theo tháng của Shop
DROP PROCEDURE IF EXISTS sp_DoanhThuShopTheoThang;
DELIMITER $$
CREATE PROCEDURE sp_DoanhThuShopTheoThang(IN p_ma_shop INT)
BEGIN
    SELECT 
        YEAR(dh.ngay_dat) AS nam,
        MONTH(dh.ngay_dat) AS thang,
        COUNT(dh.madh) AS tong_don_hang,
        COALESCE(SUM(dh.tong_tien), 0) AS tong_doanh_thu,
        COALESCE(SUM(dh.tien_phi_san), 0) AS tong_phi_san,
        COALESCE(SUM(dh.tien_thuc_nhan_shop), 0) AS thuc_nhan_shop
    FROM don_hang dh
    WHERE dh.ma_shop = p_ma_shop
      AND dh.trang_thai IN ('DaGiao', 'DA_GIAO')
    GROUP BY YEAR(dh.ngay_dat), MONTH(dh.ngay_dat)
    ORDER BY nam DESC, thang DESC;
END$$
DELIMITER ;
