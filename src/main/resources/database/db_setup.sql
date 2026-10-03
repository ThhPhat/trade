CREATE DATABASE IF NOT EXISTS QL_NhaSach DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QL_NhaSach;

create table if not exists danh_muc
(
    ma_danh_muc  int auto_increment
        primary key,
    mo_ta        varchar(500) null,
    ten_danh_muc varchar(100) not null,
    trang_thai   bit          not null,
    constraint UK5cmp5hxtq0cc1p5u3ofiser6v
        unique (ten_danh_muc)
)
    collate = utf8mb4_unicode_ci;

create table if not exists kho
(
    ma_kho        int auto_increment
        primary key,
    dia_chi       varchar(255) null,
    ghi_chu       varchar(500) null,
    ngay_tao      datetime(6)  not null,
    so_dien_thoai varchar(20)  null,
    ten_kho       varchar(100) not null,
    trang_thai    varchar(20)  not null
);

create table if not exists khuyen_mai
(
    makm             int auto_increment
        primary key,
    don_toi_thieu    int          null,
    gia_tri_giam     int          not null,
    giam_toi_da      int          null,
    loai_giam        varchar(20)  not null,
    ma_code          varchar(50)  null,
    ngay_bat_dau     datetime(6)  not null,
    ngay_ket_thuc    datetime(6)  not null,
    so_luong_da_dung int          not null,
    so_luong_toi_da  int          null,
    tenkm            varchar(150) not null,
    trang_thai       varchar(20)  not null,
    constraint UKq8resr8h2u6wfhtgxq4itxssf
        unique (ma_code)
)
    collate = utf8mb4_unicode_ci;

create table if not exists nha_cung_cap
(
    mancc         int auto_increment
        primary key,
    dia_chi       varchar(255) null,
    email         varchar(100) null,
    ma_so_thue    varchar(50)  null,
    so_dien_thoai varchar(20)  null,
    tenncc        varchar(150) not null,
    trang_thai    varchar(20)  not null
)
    collate = utf8mb4_unicode_ci;

create table if not exists nha_xuat_ban
(
    manxb         int auto_increment
        primary key,
    dia_chi       varchar(255) null,
    email         varchar(100) null,
    so_dien_thoai varchar(20)  null,
    tennxb        varchar(150) not null
)
    collate = utf8mb4_unicode_ci;

create table if not exists san_pham
(
    masp              int auto_increment
        primary key,
    isbn              varchar(20)   null,
    gia_ban           int           not null,
    loaisp            varchar(30)   not null,
    mo_ta             varchar(1000) null,
    muc_ton_toi_thieu int           not null,
    ngay_tao          datetime(6)   not null,
    so_luong_ton      int           not null,
    tensp             varchar(200)  not null,
    trang_thai        varchar(20)   not null,
    ma_danh_muc       int           not null,
    mancc             int           null,
    manxb             int           null,
    constraint UK400ysr2y7n5m2ehwyqnxb741v
        unique (isbn),
    constraint FKa0fvb13fnkejnd3vtmyicq5x7
        foreign key (manxb) references nha_xuat_ban (manxb),
    constraint FKhp2k7qqhwp3hb66f900uc5gg1
        foreign key (mancc) references nha_cung_cap (mancc),
    constraint FKqss6n6gtx6lhb7flcka9un18t
        foreign key (ma_danh_muc) references danh_muc (ma_danh_muc)
)
    collate = utf8mb4_unicode_ci;

create table if not exists kho_hang
(
    ma_kho_hang   int auto_increment
        primary key,
    muc_toi_da    int         not null,
    muc_toi_thieu int         not null,
    ngay_cap_nhat datetime(6) not null,
    so_luong_ton  int         not null,
    ma_kho        int         not null,
    masp          int         not null,
    constraint UKsjpgc3qiiuvu0ajw9vmm39mgn
        unique (ma_kho, masp),
    constraint FK4kal9sxy7w4k142gc8dh5fdtq
        foreign key (ma_kho) references kho (ma_kho),
    constraint FKl48mosg36hr2h8c41w0q24pit
        foreign key (masp) references san_pham (masp)
);

create table if not exists tac_gia
(
    ma_tac_gia  int auto_increment
        primary key,
    mo_ta       varchar(500) null,
    ten_tac_gia varchar(150) not null
)
    collate = utf8mb4_unicode_ci;

create table if not exists san_pham_tac_gia
(
    masp           int not null,
    ma_tac_gia     int not null,
    thu_tu_tac_gia int null,
    primary key (masp, ma_tac_gia),
    constraint FKd0vf47jmbbc5xd458vmmt4ly4
        foreign key (ma_tac_gia) references tac_gia (ma_tac_gia),
    constraint FKrn0c9qwvmpesdr9s0akt6lame
        foreign key (masp) references san_pham (masp)
)
    collate = utf8mb4_unicode_ci;

create table if not exists tai_khoan
(
    ma_tai_khoan  int auto_increment
        primary key,
    email         varchar(100) not null,
    mat_khau_hash varchar(255) not null,
    ngay_tao      datetime(6)  not null,
    ten_dang_nhap varchar(50)  not null,
    trang_thai    varchar(20)  not null,
    vai_tro       varchar(30)  not null,
    auth_provider varchar(20)  null,
    provider_id   varchar(100) null,
    constraint UKd0golrlr34gkql6so1i4gbuw5
        unique (email),
    constraint UKgkh4qh51gkiu8ccu1ybn1q7h7
        unique (ten_dang_nhap)
)
    collate = utf8mb4_unicode_ci;

create table if not exists khach_hang
(
    makh          int auto_increment
        primary key,
    dia_chi       varchar(255) null,
    email         varchar(100) null,
    ho_ten        varchar(100) not null,
    ngay_dang_ky  datetime(6)  not null,
    so_dien_thoai varchar(20)  null,
    ma_tai_khoan  int          null,
    anh_dai_dien  varchar(500) null,
    constraint UK6j1oks4nrqpqnl0b6cnp85vrd
        unique (so_dien_thoai),
    constraint UKiv6nhi0meph4iaotgx5h0yg63
        unique (ma_tai_khoan),
    constraint FKchhnalcpr9cvc1leppvfftoh5
        foreign key (ma_tai_khoan) references tai_khoan (ma_tai_khoan)
)
    collate = utf8mb4_unicode_ci;

create table if not exists don_hang
(
    madh               int auto_increment
        primary key,
    dia_chi_giao       varchar(255) not null,
    ly_do_huy          varchar(500) null,
    ly_do_tu_choi      varchar(500) null,
    ngay_dat           datetime(6)  not null,
    ngay_hoan_thanh    datetime(6)  null,
    ngay_xac_nhan      datetime(6)  null,
    so_dien_thoai_giao varchar(20)  not null,
    tien_giam          int          not null,
    tong_tien          int          not null,
    trang_thai         varchar(30)  not null,
    makh               int          not null,
    makm               int          null,
    constraint FKiy9wbkgc3iv3ome6new025n9o
        foreign key (makh) references khach_hang (makh),
    constraint FKmailjslygm19yf3a0vxln8tx6
        foreign key (makm) references khuyen_mai (makm)
)
    collate = utf8mb4_unicode_ci;

create table if not exists chi_tiet_don_hang
(
    madh     int not null,
    masp     int not null,
    don_gia  int not null,
    so_luong int not null,
    primary key (madh, masp),
    constraint FK98u87ds1aj1339w293i4er4fx
        foreign key (masp) references san_pham (masp),
    constraint FKodihdwuetirdvsli7a1oobwnq
        foreign key (madh) references don_hang (madh)
)
    collate = utf8mb4_unicode_ci;

create table if not exists danh_gia
(
    ma_danh_gia   int auto_increment
        primary key,
    ngay_danh_gia datetime(6)   not null,
    noi_dung      varchar(1000) null,
    so_sao        int           not null,
    trang_thai    varchar(20)   not null,
    madh          int           not null,
    makh          int           not null,
    masp          int           not null,
    constraint UKqc20lbrx414frax35ygtqldwh
        unique (makh, masp, madh),
    constraint FK1s2u0uipc62t77xq81xf9lbed
        foreign key (makh) references khach_hang (makh),
    constraint FK2x7pqo1qrotk9d8umuemqa3gi
        foreign key (madh) references don_hang (madh),
    constraint FK6ngu0jah0wdv2nk6bsxcwygh9
        foreign key (masp) references san_pham (masp)
)
    collate = utf8mb4_unicode_ci;

create table if not exists gio_hang
(
    ma_gio_hang   int auto_increment
        primary key,
    ngay_cap_nhat datetime(6) not null,
    ngay_tao      datetime(6) not null,
    makh          int         not null,
    constraint FKr918hcrsa82ly9duc4jsec5n8
        foreign key (makh) references khach_hang (makh)
)
    collate = utf8mb4_unicode_ci;

create table if not exists chi_tiet_gio_hang
(
    ma_gio_hang int not null,
    masp        int not null,
    so_luong    int not null,
    primary key (ma_gio_hang, masp),
    constraint FKdqjc7vkyfiup4ydgmhx8sdwii
        foreign key (ma_gio_hang) references gio_hang (ma_gio_hang),
    constraint FKkmq7k0m84x6j0g74rxs4fw0wg
        foreign key (masp) references san_pham (masp)
)
    collate = utf8mb4_unicode_ci;

create table if not exists nhan_vien
(
    manv          int auto_increment
        primary key,
    chuc_vu       varchar(30)  not null,
    dia_chi       varchar(255) null,
    email         varchar(100) null,
    ho_ten        varchar(100) not null,
    ngay_vao_lam  date         null,
    so_dien_thoai varchar(20)  not null,
    trang_thai    varchar(20)  not null,
    ma_tai_khoan  int          null,
    constraint UKetdcxme7ynbys36hi28u9tk4e
        unique (so_dien_thoai),
    constraint UKs931jur9i7px0jt36iev3osli
        unique (ma_tai_khoan),
    constraint FKdpk3u6xuawsiksnkklx1pfeyw
        foreign key (ma_tai_khoan) references tai_khoan (ma_tai_khoan)
)
    collate = utf8mb4_unicode_ci;

create table if not exists hoa_don
(
    mahd                 int auto_increment
        primary key,
    ngay_lap             datetime(6) not null,
    tong_tien_thanh_toan int         not null,
    trang_thai           varchar(20) not null,
    madh                 int         not null,
    manv                 int         not null,
    constraint UKi5dsqpkn4sb6xnm62vxche09x
        unique (madh),
    constraint FK83d5kek5uoh8e1kralry52seo
        foreign key (madh) references don_hang (madh),
    constraint FKr67k5gttxaonk5trdfwvcgk80
        foreign key (manv) references nhan_vien (manv)
)
    collate = utf8mb4_unicode_ci;

create table if not exists password_reset_token
(
    id           bigint auto_increment
        primary key,
    expiry_date  datetime(6)  not null,
    ngay_tao     datetime(6)  not null,
    token        varchar(100) not null,
    used         bit          not null,
    ma_tai_khoan int          not null,
    otp_code     varchar(10)  not null,
    constraint UKg0guo4k8krgpwuagos61oc06j
        unique (token),
    constraint FK9ym08qkf9wlr5jc1kclfmx34t
        foreign key (ma_tai_khoan) references tai_khoan (ma_tai_khoan)
);

create table if not exists phieu_kiem_ke
(
    ma_phieu_kiem_ke int auto_increment
        primary key,
    ghi_chu          varchar(500) null,
    ngay_kiem_ke     datetime(6)  not null,
    ngay_phe_duyet   datetime(6)  null,
    trang_thai       varchar(30)  not null,
    manv             int          not null,
    manvphe_duyet    int          null,
    ma_kho           int          not null,
    constraint FKgpc5xjviuff0encds3s0sitay
        foreign key (ma_kho) references kho (ma_kho),
    constraint FKivhhrnwvje1osre9o5o97ihxj
        foreign key (manv) references nhan_vien (manv),
    constraint FKmaqnbjq6sj13w1q9ttu5pby52
        foreign key (manvphe_duyet) references nhan_vien (manv)
)
    collate = utf8mb4_unicode_ci;

create table if not exists chi_tiet_kiem_ke
(
    ma_phieu_kiem_ke  int          not null,
    masp              int          not null,
    ly_do             varchar(500) null,
    so_luong_he_thong int          not null,
    so_luong_thuc_te  int          not null,
    primary key (ma_phieu_kiem_ke, masp),
    constraint FKghnffbkpvpnp3ahq0g43y7e0g
        foreign key (masp) references san_pham (masp),
    constraint FKkc6e2jvl6hvhqtm42mr6xfebi
        foreign key (ma_phieu_kiem_ke) references phieu_kiem_ke (ma_phieu_kiem_ke)
)
    collate = utf8mb4_unicode_ci;

create table if not exists phieu_nhap
(
    mapn       int auto_increment
        primary key,
    ghi_chu    varchar(500) null,
    ngay_nhap  datetime(6)  not null,
    tong_tien  int          not null,
    trang_thai varchar(20)  not null,
    mancc      int          not null,
    manv       int          not null,
    ma_kho     int          not null,
    constraint FKbw2u0ber2va865kg948efhlm5
        foreign key (mancc) references nha_cung_cap (mancc),
    constraint FKlavt7ihi8dug436gh1vq69w1y
        foreign key (manv) references nhan_vien (manv),
    constraint FKqtsuyyf14bh4lge186fqfh79h
        foreign key (ma_kho) references kho (ma_kho)
)
    collate = utf8mb4_unicode_ci;

create table if not exists chi_tiet_phieu_nhap
(
    mapn         int not null,
    masp         int not null,
    don_gia_nhap int not null,
    so_luong     int not null,
    primary key (mapn, masp),
    constraint FKdjxtx10fxow826rs8ju4rlcvq
        foreign key (masp) references san_pham (masp),
    constraint FKkl3e6ypo8mcm8st8axnq3yj4g
        foreign key (mapn) references phieu_nhap (mapn)
)
    collate = utf8mb4_unicode_ci;

create table if not exists phieu_tra_hang
(
    ma_phieu_tra int auto_increment
        primary key,
    ghi_chu      varchar(500) null,
    ly_do        varchar(500) not null,
    ngay_tra     datetime(6)  not null,
    trang_thai   varchar(20)  not null,
    mancc        int          not null,
    manv         int          not null,
    constraint FK4l2i3xyg39ixq625uf8pght9o
        foreign key (mancc) references nha_cung_cap (mancc),
    constraint FKjat51cayratbgicvq12d7r800
        foreign key (manv) references nhan_vien (manv)
)
    collate = utf8mb4_unicode_ci;

create table if not exists chi_tiet_tra_hang
(
    ma_phieu_tra int not null,
    masp         int not null,
    don_gia      int not null,
    so_luong     int not null,
    primary key (ma_phieu_tra, masp),
    constraint FK3dy6w1ltkf3jmrrqk34axdsxt
        foreign key (ma_phieu_tra) references phieu_tra_hang (ma_phieu_tra),
    constraint FK78org0kyuit4uqye0lwy15bu
        foreign key (masp) references san_pham (masp)
)
    collate = utf8mb4_unicode_ci;

create table if not exists thanh_toan
(
    ma_thanh_toan   int auto_increment
        primary key,
    ma_giao_dich    varchar(100) null,
    ngay_thanh_toan datetime(6)  null,
    noi_dung        varchar(500) null,
    phuong_thuc     varchar(30)  not null,
    so_tien         int          not null,
    trang_thai      varchar(30)  not null,
    madh            int          not null,
    constraint FKfo8a50ev7l24cccqm3v0hfwbv
        foreign key (madh) references don_hang (madh)
)
    collate = utf8mb4_unicode_ci;

create table if not exists voucher_da_luu
(
    id bigint auto_increment primary key,
    ngay_luu datetime(6) not null,
    ngay_su_dung datetime(6) null,
    trang_thai varchar(20) not null,
    makh int not null,
    makm int not null,
    constraint FK_vdl_kh foreign key (makh) references khach_hang (makh),
    constraint FK_vdl_km foreign key (makm) references khuyen_mai (makm)
) collate = utf8mb4_unicode_ci;


-- ==========================================
-- PH?N B? SUNG: INDEXES, FUNCTIONS V� STORED PROCEDURES
-- ==========================================

DELIMITER $$
DROP PROCEDURE IF EXISTS CreateIdxIfNotExists $$
CREATE PROCEDURE CreateIdxIfNotExists(
    IN p_idx_name VARCHAR(255),
    IN p_tbl_name VARCHAR(255),
    IN p_sql VARCHAR(1000)
)
BEGIN
    DECLARE idx_exists INT;
    SELECT COUNT(1) INTO idx_exists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = p_tbl_name
      AND index_name = p_idx_name;

    IF idx_exists = 0 THEN
        SET @stmt = p_sql;
        PREPARE s FROM @stmt;
        EXECUTE s;
        DEALLOCATE PREPARE s;
    END IF;
END$$
DELIMITER ;

-- =========================================================================
-- THE4BOOKSTORE - TỔNG HỢP CÁC INDEX TỐI ƯU HIỆU NĂNG DATABASE
-- =========================================================================
-- Chú thích:
-- 1. Các cột PRIMARY KEY và UNIQUE (như ISBN, ten_dang_nhap, email) đã được MySQL 
--    tự động tạo B-Tree Index mặc định.
-- 2. Dưới đây là các INDEX bổ sung cho các trường hay dùng trong WHERE, ORDER BY, JOIN.
-- 3. Sử dụng lệnh CREATE INDEX IF NOT EXISTS (MySQL 8.0+) hoặc chạy trực tiếp.
-- =========================================================================

-- 1. BẢNG SAN_PHAM (Sản phẩm - Tần suất truy vấn và lọc cao nhất)
-- Tối ưu lọc theo loại sản phẩm và trạng thái bán (vd: Sách đang bán)


-- Tối ưu lọc theo danh mục cụ thể và trạng thái bán


-- Tối ưu tìm kiếm theo tên sản phẩm


-- Tối ưu sắp xếp sản phẩm mới nhất (ORDER BY ngay_tao DESC)


-- Tối ưu sắp xếp theo giá bán tăng/giảm (ORDER BY gia_ban)



-- 2. BẢNG DANH_MUC (Danh mục sản phẩm)
-- Tối ưu lọc các danh mục đang hoạt động (WHERE trang_thai = true)
CREATE INDEX idx_dm_trangthai ON danh_muc(trang_thai);


-- 3. BẢNG DON_HANG (Đơn hàng)
-- Tối ưu lọc đơn hàng theo trạng thái (ChoXuLy, DangGiao, HoanThanh...)
CREATE INDEX idx_dh_trangthai ON don_hang(trang_thai);

-- Tối ưu truy vấn lịch sử đơn hàng theo ngày đặt và thống kê doanh thu theo tháng
CREATE INDEX idx_dh_ngaydat ON don_hang(ngay_dat);

-- Tối ưu tìm kiếm đơn hàng của từng khách hàng
CREATE INDEX idx_dh_makh ON don_hang(makh);


-- 4. BẢNG TAI_KHOAN (Tài khoản người dùng)
-- Tối ưu lọc tài khoản theo vai trò (ADMIN, QUANLY, KHACHHANG...) và trạng thái
CREATE INDEX idx_tk_vaitro_trangthai ON tai_khoan(vai_tro, trang_thai);


-- 5. BẢNG PHIEU_NHAP & PHIEU_KIEM_KE (Kho hàng)
-- Tối ưu lọc và sắp xếp phiếu nhập theo ngày nhập
CREATE INDEX idx_pn_ngaynhap ON phieu_nhap(ngay_nhap);
CREATE INDEX idx_pn_trangthai ON phieu_nhap(trang_thai);

-- Tối ưu lọc và sắp xếp phiếu kiểm kê theo ngày tạo
CREATE INDEX idx_pkk_ngaykiemke ON phieu_kiem_ke(ngay_kiem_ke);
CREATE INDEX idx_pkk_trangthai ON phieu_kiem_ke(trang_thai);


-- =========================================================================
-- LỆNH KIỂM TRA CÁC INDEX HIỆN CÓ TRONG BẢNG:
-- SHOW INDEX FROM san_pham;
-- SHOW INDEX FROM danh_muc;
-- SHOW INDEX FROM don_hang;
-- =========================================================================
-- 6. BẢNG KHO_HANG (Tối ưu tra cứu tồn kho)
CALL CreateIdxIfNotExists('idx_kh_sanpham_kho', 'kho_hang', 'CREATE INDEX idx_kh_sanpham_kho ON kho_hang(masp, ma_kho)');
CALL CreateIdxIfNotExists('idx_kh_tonkho_toithieu', 'kho_hang', 'CREATE INDEX idx_kh_tonkho_toithieu ON kho_hang(so_luong_ton, muc_toi_thieu)');

-- 7. BẢNG DON_HANG (Bổ sung cho thống kê doanh thu)
CALL CreateIdxIfNotExists('idx_dh_ngayhoanthanh', 'don_hang', 'CREATE INDEX idx_dh_ngayhoanthanh ON don_hang(ngay_hoan_thanh)');
CALL CreateIdxIfNotExists('idx_dh_makm', 'don_hang', 'CREATE INDEX idx_dh_makm ON don_hang(makm)');

-- 8. BẢNG CHI_TIET_DON_HANG
CALL CreateIdxIfNotExists('idx_ctdh_madh', 'chi_tiet_don_hang', 'CREATE INDEX idx_ctdh_madh ON chi_tiet_don_hang(madh)');
CALL CreateIdxIfNotExists('idx_ctdh_masp', 'chi_tiet_don_hang', 'CREATE INDEX idx_ctdh_masp ON chi_tiet_don_hang(masp)');

-- 9. BẢNG GIO_HANG & CHI_TIET_GIO_HANG
CALL CreateIdxIfNotExists('idx_gh_makh', 'gio_hang', 'CREATE INDEX idx_gh_makh ON gio_hang(makh)');
CALL CreateIdxIfNotExists('idx_ctgh_magiohang_masp', 'chi_tiet_gio_hang', 'CREATE INDEX idx_ctgh_magiohang_masp ON chi_tiet_gio_hang(ma_gio_hang, masp)');

-- 10. BẢNG KHUYEN_MAI
CALL CreateIdxIfNotExists('idx_km_macode', 'khuyen_mai', 'CREATE INDEX idx_km_macode ON khuyen_mai(ma_code)');

-- 11. BẢNG SAN_PHAM_TAC_GIA
CALL CreateIdxIfNotExists('idx_sptg_masp', 'san_pham_tac_gia', 'CREATE INDEX idx_sptg_masp ON san_pham_tac_gia(masp)');
CALL CreateIdxIfNotExists('idx_sptg_matacgia', 'san_pham_tac_gia', 'CREATE INDEX idx_sptg_matacgia ON san_pham_tac_gia(ma_tac_gia)');

-- 12. BẢNG PASSWORD_RESET_TOKEN
CALL CreateIdxIfNotExists('idx_prt_matk_used', 'password_reset_token', 'CREATE INDEX idx_prt_matk_used ON password_reset_token(ma_tai_khoan, used)');

-- 13. BẢNG VOUCHER_DA_LUU
CALL CreateIdxIfNotExists('idx_vdl_makh_trangthai', 'voucher_da_luu', 'CREATE INDEX idx_vdl_makh_trangthai ON voucher_da_luu(makh, trang_thai)');
CALL CreateIdxIfNotExists('idx_vdl_makh_makm', 'voucher_da_luu', 'CREATE INDEX idx_vdl_makh_makm ON voucher_da_luu(makh, makm)');

-- 14. BẢNG SAN_PHAM (Bổ sung cho tìm kiếm theo NXB)


-- 15. BẢNG THANH_TOAN
CALL CreateIdxIfNotExists('idx_tt_madh', 'thanh_toan', 'CREATE INDEX idx_tt_madh ON thanh_toan(madh)');
-- phase 3: MySQL functions and stored procedures for READ operations

DROP FUNCTION IF EXISTS fn_TinhDoanhThu;
DELIMITER $$
CREATE FUNCTION fn_TinhDoanhThu(startDate DATETIME, endDate DATETIME) 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE total BIGINT DEFAULT 0;
    -- Tính tổng doanh thu từ các đơn hàng đã giao thành công trong khoảng thời gian
    SELECT COALESCE(SUM(tong_tien), 0) INTO total
    FROM don_hang
    WHERE trang_thai = 'DaGiao' 
      AND ngay_hoan_thanh >= startDate 
      AND ngay_hoan_thanh <= endDate;
    RETURN total;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_DemDonHang;
DELIMITER $$
CREATE FUNCTION fn_DemDonHang(startDate DATETIME, endDate DATETIME) 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    -- Đếm số lượng đơn hàng được đặt trong khoảng thời gian
    SELECT COUNT(madh) INTO cnt
    FROM don_hang
    WHERE ngay_dat >= startDate AND ngay_dat <= endDate;
    RETURN cnt;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_TongSachDaBan;
DELIMITER $$
CREATE FUNCTION fn_TongSachDaBan() 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE total BIGINT DEFAULT 0;
    -- Tính tổng số lượng sách đã bán từ các đơn hàng đã giao
    SELECT COALESCE(SUM(ct.so_luong), 0) INTO total
    FROM chi_tiet_don_hang ct
    JOIN don_hang dh ON ct.madh = dh.madh
    WHERE dh.trang_thai = 'DaGiao';
    RETURN total;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_DoanhThuTheoThang;
DELIMITER $$
CREATE PROCEDURE sp_DoanhThuTheoThang()
BEGIN
    -- Lấy doanh thu theo 12 tháng gần nhất
    SELECT YEAR(dh.ngay_hoan_thanh) as nam, 
           MONTH(dh.ngay_hoan_thanh) as thang, 
           SUM(dh.tong_tien) as doanh_thu 
    FROM don_hang dh 
    WHERE dh.trang_thai = 'DaGiao' 
      AND dh.ngay_hoan_thanh IS NOT NULL 
    GROUP BY YEAR(dh.ngay_hoan_thanh), MONTH(dh.ngay_hoan_thanh) 
    ORDER BY nam DESC, thang DESC 
    LIMIT 12;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_TopSachBanChay;
DELIMITER $$
CREATE PROCEDURE sp_TopSachBanChay(IN p_limit INT)
BEGIN
    -- Lấy top sách bán chạy nhất
    SELECT s.masp, s.tensp, SUM(ct.so_luong) as tong_da_ban
      FROM san_pham s
      JOIN chi_tiet_don_hang ct ON s.masp = ct.masp
    JOIN don_hang dh ON ct.madh = dh.madh
    WHERE dh.trang_thai = 'DaGiao'
    GROUP BY s.masp, s.tensp
    ORDER BY tong_da_ban DESC
    LIMIT p_limit;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_DemSanPhamSapHet;
DELIMITER $$
CREATE FUNCTION fn_DemSanPhamSapHet() 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    -- Đếm số lượng sách sắp hết hàng (tồn kho từ 1 đến 10)
    SELECT COUNT(masp) INTO cnt
    FROM san_pham
    WHERE so_luong_ton > 0 AND so_luong_ton <= 10;
    RETURN cnt;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_DemSanPhamHetHang;
DELIMITER $$
CREATE FUNCTION fn_DemSanPhamHetHang() 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    -- Đếm số lượng sách đã hết hàng (tồn kho <= 0)
    SELECT COUNT(masp) INTO cnt
    FROM san_pham
    WHERE so_luong_ton <= 0;
    RETURN cnt;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_TinhTienGiamKhuyenMai;
DELIMITER $$
CREATE FUNCTION fn_TinhTienGiamKhuyenMai(p_makm VARCHAR(50)) 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE total_discount BIGINT DEFAULT 0;
    -- Tính tổng số tiền đã giảm từ các đơn hàng sử dụng mã khuyến mãi
    SELECT COALESCE(SUM(tien_giam_gia), 0) INTO total_discount
    FROM don_hang
    WHERE makm = p_makm AND trang_thai = 'DaGiao';
    RETURN total_discount;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_TinhDoanhThuKhuyenMai;
DELIMITER $$
CREATE FUNCTION fn_TinhDoanhThuKhuyenMai(p_makm VARCHAR(50)) 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE total BIGINT DEFAULT 0;
    -- Tính tổng doanh thu từ các đơn hàng sử dụng mã khuyến mãi
    SELECT COALESCE(SUM(tong_tien), 0) INTO total
    FROM don_hang
    WHERE makm = p_makm AND trang_thai = 'DaGiao';
    RETURN total;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_DemSachTheoTacGia;
DELIMITER $$
CREATE FUNCTION fn_DemSachTheoTacGia(p_matacgia INT) 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    -- Đếm số đầu sách của một tác giả
    SELECT COUNT(masp) INTO cnt
    FROM san_pham_tac_gia
    WHERE ma_tac_gia = p_matacgia;
    RETURN cnt;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_DemSachTheoNXB;
DELIMITER $$
CREATE FUNCTION fn_DemSachTheoNXB(p_manxb INT) 
RETURNS BIGINT
READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    -- Đếm số đầu sách của một nhà xuất bản
    SELECT COUNT(masp) INTO cnt
    FROM san_pham
    WHERE manxb = p_manxb;
    RETURN cnt;
END $$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_KiemTraOTPConHieuLuc;
DELIMITER $$
CREATE FUNCTION fn_KiemTraOTPConHieuLuc(p_email VARCHAR(255)) 
RETURNS TINYINT
READS SQL DATA
BEGIN
    DECLARE is_valid TINYINT DEFAULT 0;
    -- Kiểm tra OTP còn hiệu lực đối với email này hay không
    SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END INTO is_valid
    FROM tai_khoan
    WHERE email = p_email 
      AND otp_expiry_time > NOW();
    RETURN is_valid;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_LayDanhMucTheoLoaiSP;
DELIMITER $$
CREATE PROCEDURE sp_LayDanhMucTheoLoaiSP(IN p_loaisp INT)
BEGIN
    -- Lấy danh sách các danh mục theo loại sản phẩm
    SELECT madanhmuc, tendanhmuc, loaisp
    FROM danh_muc
    WHERE loaisp = p_loaisp;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_TimKiemKhuyenMai;
DELIMITER $$
CREATE PROCEDURE sp_TimKiemKhuyenMai(IN p_keyword VARCHAR(255), IN p_loaigiam VARCHAR(50))
BEGIN
    -- Tìm kiếm khuyến mãi theo từ khóa và loại giảm giá
    SELECT makm, tenkm, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, trang_thai
    FROM khuyen_mai
    WHERE (p_keyword IS NULL OR tenkm LIKE CONCAT('%', p_keyword, '%') OR makm LIKE CONCAT('%', p_keyword, '%'))
      AND (p_loaigiam IS NULL OR loai_giam = p_loaigiam);
END $$
DELIMITER ;
DELIMITER $$

-- 1. Cập nhật trạng thái đơn hàng
CREATE PROCEDURE sp_CapNhatTrangThaiDonHang(
    IN p_madh INT,
    IN p_trangthaimoi VARCHAR(50),
    IN p_lydo VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_trangthaimoi = 'TU_CHOI' THEN
        UPDATE don_hang 
        SET trang_thai = p_trangthaimoi, ly_do_tu_choi = p_lydo 
        WHERE madh = p_madh;
    ELSEIF p_trangthaimoi = 'DA_HUY' THEN
        UPDATE don_hang 
        SET trang_thai = p_trangthaimoi, ly_do_huy = p_lydo 
        WHERE madh = p_madh;
    ELSE
        UPDATE don_hang 
        SET trang_thai = p_trangthaimoi 
        WHERE madh = p_madh;
    END IF;

    COMMIT;
END$$

-- 2. Hủy đơn hàng và hoàn lại số lượng tồn kho
CREATE PROCEDURE sp_HuyDonHang(
    IN p_madh INT,
    IN p_makh INT,
    IN p_lydohuy VARCHAR(255)
)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_masp INT;
    DECLARE v_soluong INT;
    DECLARE v_trangthai VARCHAR(50);
    
    DECLARE cur_ctdh CURSOR FOR 
        SELECT masp, so_luong FROM chi_tiet_don_hang WHERE madh = p_madh;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Lấy trạng thái hiện tại (có khóa dòng)
    SELECT trang_thai INTO v_trangthai 
    FROM don_hang 
    WHERE madh = p_madh AND makh = p_makh 
    FOR UPDATE;
    
    IF v_trangthai != 'DA_HUY' AND v_trangthai != 'DA_GIAO' THEN
        -- Hoàn lại số lượng tồn kho cho từng sản phẩm
        OPEN cur_ctdh;
        read_loop: LOOP
            FETCH cur_ctdh INTO v_masp, v_soluong;
            IF done THEN
                LEAVE read_loop;
            END IF;
            
            UPDATE san_pham 
            SET so_luong_ton = so_luong_ton + v_soluong 
            WHERE masp = v_masp;
        END LOOP;
        CLOSE cur_ctdh;
        
        -- Cập nhật trạng thái đơn hàng thành đã hủy
        UPDATE don_hang 
        SET trang_thai = 'DA_HUY', ly_do_huy = p_lydohuy 
        WHERE madh = p_madh AND makh = p_makh;
    END IF;

    COMMIT;
END$$

-- 3. Xóa phiếu nhập (xóa chi tiết trước rồi mới xóa phiếu nhập)
CREATE PROCEDURE sp_XoaPhieuNhap(
    IN p_mapn INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Xóa chi tiết phiếu nhập trước
    DELETE FROM chi_tiet_phieu_nhap WHERE mapn = p_mapn;
    
    -- Sau đó xóa phiếu nhập
    DELETE FROM phieu_nhap WHERE mapn = p_mapn;

    COMMIT;
END$$

-- 4. Thêm sản phẩm vào giỏ hàng
CREATE PROCEDURE sp_ThemVaoGioHang(
    IN p_makh INT,
    IN p_masp INT,
    IN p_soluong INT
)
BEGIN
    DECLARE v_count INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Kiểm tra sản phẩm đã có trong giỏ hàng chưa
    SELECT COUNT(*) INTO v_count 
    FROM gio_hang 
    WHERE makh = p_makh AND masp = p_masp;

    IF v_count > 0 THEN
        -- Cập nhật số lượng nếu sản phẩm đã có trong giỏ
        UPDATE gio_hang 
        SET so_luong = so_luong + p_soluong 
        WHERE makh = p_makh AND masp = p_masp;
    ELSE
        -- Thêm mới vào giỏ hàng
        INSERT INTO gio_hang (makh, masp, so_luong) 
        VALUES (p_makh, p_masp, p_soluong);
    END IF;

    COMMIT;
END$$

-- 5. Xóa sản phẩm khỏi giỏ hàng
CREATE PROCEDURE sp_XoaKhoiGioHang(
    IN p_makh INT,
    IN p_masp INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DELETE FROM gio_hang 
    WHERE makh = p_makh AND masp = p_masp;

    COMMIT;
END$$

DELIMITER ;

-- =================================================================================
-- 6. sp_XacNhanPhieuNhap
-- Xác nhận phiếu nhập, cập nhật số lượng tồn kho và đổi trạng thái phiếu nhập
-- =================================================================================
DELIMITER $$
CREATE PROCEDURE sp_XacNhanPhieuNhap(IN p_mapn INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_masp INT;
    DECLARE v_soluong INT;
    -- Con trỏ để lặp qua các chi tiết phiếu nhập
    DECLARE cur CURSOR FOR 
        SELECT san_pham_id, so_luong 
        FROM chi_tiet_phieu_nhap 
        WHERE phieu_nhap_id = p_mapn;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_masp, v_soluong;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Cập nhật số lượng tồn kho
        UPDATE kho_hang 
        SET so_luong_ton = so_luong_ton + v_soluong 
        WHERE san_pham_id = v_masp;
    END LOOP;
    CLOSE cur;
    
    -- Cập nhật trạng thái phiếu nhập thành 'DaNhap'
    UPDATE phieu_nhap 
    SET trang_thai = 'DaNhap' 
    WHERE id = p_mapn;
    
    COMMIT;
END$$
DELIMITER ;

-- =================================================================================
-- 7. sp_XacNhanPhieuKiemKe
-- Xác nhận phiếu kiểm kê, cập nhật số lượng tồn kho bằng số lượng thực tế
-- =================================================================================
DELIMITER $$
CREATE PROCEDURE sp_XacNhanPhieuKiemKe(IN p_mapk INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_masp INT;
    DECLARE v_soluong_thucte INT;
    -- Con trỏ để lặp qua các chi tiết phiếu kiểm kê
    DECLARE cur CURSOR FOR 
        SELECT san_pham_id, so_luong_thuc_te 
        FROM chi_tiet_kiem_ke 
        WHERE phieu_kiem_ke_id = p_mapk;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_masp, v_soluong_thucte;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Cập nhật số lượng tồn kho theo số lượng thực tế kiểm kê
        UPDATE kho_hang 
        SET so_luong_ton = v_soluong_thucte 
        WHERE san_pham_id = v_masp;
    END LOOP;
    CLOSE cur;
    
    -- Cập nhật trạng thái phiếu kiểm kê thành 'DaDuyet'
    UPDATE phieu_kiem_ke 
    SET trang_thai = 'DaDuyet' 
    WHERE id = p_mapk;
    
    COMMIT;
END$$
DELIMITER ;

-- =================================================================================
-- 8. sp_CapNhatGioHang
-- Cập nhật số lượng sản phẩm trong giỏ hàng hoặc xóa nếu số lượng <= 0
-- =================================================================================
DELIMITER $$
CREATE PROCEDURE sp_CapNhatGioHang(IN p_makh INT, IN p_masp INT, IN p_soluongmoi INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    IF p_soluongmoi <= 0 THEN
        -- Xóa sản phẩm khỏi giỏ hàng
        DELETE FROM gio_hang 
        WHERE khach_hang_id = p_makh AND san_pham_id = p_masp;
    ELSE
        -- Cập nhật số lượng mới
        UPDATE gio_hang 
        SET so_luong = p_soluongmoi 
        WHERE khach_hang_id = p_makh AND san_pham_id = p_masp;
    END IF;
    
    COMMIT;
END$$
DELIMITER ;

-- =================================================================================
-- 9. sp_XoaToanBoGioHang
-- Xóa toàn bộ sản phẩm trong giỏ hàng của một khách hàng
-- =================================================================================
DELIMITER $$
CREATE PROCEDURE sp_XoaToanBoGioHang(IN p_makh INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Xóa tất cả các sản phẩm trong giỏ hàng
    DELETE FROM gio_hang 
    WHERE khach_hang_id = p_makh;
    
    COMMIT;
END$$
DELIMITER ;

-- =================================================================================
-- 10. sp_TaoTokenResetPassword
-- Tạo mã OTP 6 số để reset mật khẩu và xóa mềm các token cũ
-- =================================================================================
DELIMITER $$
CREATE PROCEDURE sp_TaoTokenResetPassword(IN p_email VARCHAR(100), OUT p_otp VARCHAR(10))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Sinh mã OTP ngẫu nhiên 6 chữ số
    SET p_otp = LPAD(FLOOR(RAND() * 999999.99), 6, '0');
    
    -- Xóa mềm (vô hiệu hóa) các token reset cũ của người dùng này
    UPDATE password_reset_token 
    SET da_xoa = 1 
    WHERE email = p_email AND da_xoa = 0;
    
    -- Thêm token mới với thời hạn là 15 phút
    INSERT INTO password_reset_token (email, otp, thoi_gian_het_han, da_xoa, da_su_dung) 
    VALUES (p_email, p_otp, DATE_ADD(NOW(), INTERVAL 15 MINUTE), 0, 0);
    
    COMMIT;
END$$
DELIMITER ;

-- =================================================================================
-- 11. sp_ResetPassword
-- Xác thực OTP và cập nhật mật khẩu mới, đánh dấu token đã sử dụng
-- =================================================================================
DELIMITER $$
CREATE PROCEDURE sp_ResetPassword(IN p_email VARCHAR(100), IN p_otp VARCHAR(10), IN p_newpasshash VARCHAR(255))
BEGIN
    DECLARE v_valid INT DEFAULT 0;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Kiểm tra OTP có tồn tại, chưa sử dụng, chưa bị xóa và còn hạn không
    SELECT COUNT(*) INTO v_valid 
    FROM password_reset_token 
    WHERE email = p_email 
      AND otp = p_otp 
      AND da_xoa = 0 
      AND da_su_dung = 0 
      AND thoi_gian_het_han > NOW();
    
    IF v_valid > 0 THEN
        -- Cập nhật mật khẩu mới trong bảng tài khoản
        UPDATE tai_khoan 
        SET mat_khau = p_newpasshash 
        WHERE email = p_email;
        
        -- Đánh dấu token đã được sử dụng
        UPDATE password_reset_token 
        SET da_su_dung = 1 
        WHERE email = p_email AND otp = p_otp;
    ELSE
        -- Báo lỗi nếu OTP không hợp lệ
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'OTP không hợp lệ, đã được sử dụng hoặc đã hết hạn';
    END IF;
    
    COMMIT;
END$$
DELIMITER ;

CALL CreateIdxIfNotExists('idx_sp_danhmuc_trangthai', 'san_pham', 'create index idx_sp_danhmuc_trangthai on san_pham (ma_danh_muc, trang_thai)');
CALL CreateIdxIfNotExists('idx_sp_giaban', 'san_pham', 'create index idx_sp_giaban on san_pham (gia_ban)');
CALL CreateIdxIfNotExists('idx_sp_loaisp_trangthai', 'san_pham', 'create index idx_sp_loaisp_trangthai on san_pham (loaisp, trang_thai)');
CALL CreateIdxIfNotExists('idx_sp_ngaytao', 'san_pham', 'create index idx_sp_ngaytao on san_pham (ngay_tao)');
CALL CreateIdxIfNotExists('idx_sp_tensp', 'san_pham', 'create index idx_sp_tensp on san_pham (tensp)');



