-- ====================================================================
-- THE4BOOKSTORE - HỆ THỐNG CƠ SỞ DỮ LIỆU SÀN THƯƠNG MẠI ĐIỆN TỬ SÁCH
-- FILE TẠO CSDL TOÀN DIỆN VÀ MỚI NHẤT (SCHEMA + DATA + PROCEDURES + ROLES)
-- HỆ QUẢN TRỊ CSDL: MICROSOFT SQL SERVER 2022/2025 (T-SQL)
-- CẬP NHẬT: 17/10/2026
-- MẬT KHẨU MẶC ĐỊNH TẤT CẢ TÀI KHOẢN: 123456
-- ====================================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QL_NhaSach')
BEGIN
    ALTER DATABASE [QL_NhaSach] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [QL_NhaSach];
END
GO

CREATE DATABASE [QL_NhaSach] COLLATE Vietnamese_CI_AS;
GO

USE [QL_NhaSach];
GO

-- ====================================================================
-- PHẦN 1: TẠO CÁC BẢNG DỮ LIỆU & RÀNG BUỘC (TABLES & CONSTRAINTS)
-- ====================================================================

-- Bảng: tai_khoan
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[tai_khoan](
	[ma_tai_khoan] [int] IDENTITY(1,1) NOT NULL,
	[ten_dang_nhap] [varchar](50) COLLATE Vietnamese_CI_AS NOT NULL,
	[email] [varchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[mat_khau_hash] [varchar](255) COLLATE Vietnamese_CI_AS NULL,
	[vai_tro] [varchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[auth_provider] [varchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[provider_id] [varchar](100) COLLATE Vietnamese_CI_AS NULL,
	[token_version] [int] NOT NULL,
	[otp_code] [varchar](10) COLLATE Vietnamese_CI_AS NULL,
	[otp_expiry] [datetime2](7) NULL,
	[ngay_tao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [UQ_tai_khoan_email] UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [UQ_tai_khoan_username] UNIQUE NONCLUSTERED 
(
	[ten_dang_nhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[tai_khoan] ADD  CONSTRAINT [DF_tai_khoan_trangthai]  DEFAULT (N'HoatDong') FOR [trang_thai]
GO
ALTER TABLE [dbo].[tai_khoan] ADD  CONSTRAINT [DF_tai_khoan_auth]  DEFAULT ('LOCAL') FOR [auth_provider]
GO
ALTER TABLE [dbo].[tai_khoan] ADD  CONSTRAINT [DF_tai_khoan_tokenver]  DEFAULT ((1)) FOR [token_version]
GO
ALTER TABLE [dbo].[tai_khoan] ADD  CONSTRAINT [DF_tai_khoan_ngaytao]  DEFAULT (sysutcdatetime()) FOR [ngay_tao]
GO
ALTER TABLE [dbo].[tai_khoan]  WITH CHECK ADD  CONSTRAINT [CK_tai_khoan_vaitro] CHECK  (([vai_tro]='USER' OR [vai_tro]='KHACHHANG' OR [vai_tro]='VENDOR' OR [vai_tro]='MANAGER' OR [vai_tro]='ADMIN'))
GO
ALTER TABLE [dbo].[tai_khoan] CHECK CONSTRAINT [CK_tai_khoan_vaitro]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Trigger 5: Audit ghi vết thay đổi vai trò hoặc trạng thái tài khoản
CREATE   TRIGGER trg_AuditThayDoiVaiTroTaiKhoan
ON tai_khoan
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(vai_tro) OR UPDATE(trang_thai)
    BEGIN
        INSERT INTO nhat_ky_bao_mat (ten_bang, hanh_dong, ban_ghi_id, du_lieu_cu, du_lieu_moi)
        SELECT 
            'tai_khoan',
            'UPDATE',
            i.ma_tai_khoan,
            CONCAT(N'VaiTro: ', d.vai_tro, N', TrangThai: ', d.trang_thai),
            CONCAT(N'VaiTro: ', i.vai_tro, N', TrangThai: ', i.trang_thai)
        FROM inserted i
        JOIN deleted d ON i.ma_tai_khoan = d.ma_tai_khoan
        WHERE i.vai_tro != d.vai_tro OR i.trang_thai != d.trang_thai;
    END
END;

GO
ALTER TABLE [dbo].[tai_khoan] ENABLE TRIGGER [trg_AuditThayDoiVaiTroTaiKhoan]
GO

-- Bảng: khach_hang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[khach_hang](
	[makh] [int] IDENTITY(1,1) NOT NULL,
	[ma_tai_khoan] [int] NULL,
	[ho_ten] [nvarchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[email] [varchar](100) COLLATE Vietnamese_CI_AS NULL,
	[so_dien_thoai] [varchar](20) COLLATE Vietnamese_CI_AS NULL,
	[dia_chi] [nvarchar](255) COLLATE Vietnamese_CI_AS NULL,
	[anh_dai_dien] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[ngay_dang_ky] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[makh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[ma_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[khach_hang] ADD  CONSTRAINT [DF_khachhang_ngaydangky]  DEFAULT (sysutcdatetime()) FOR [ngay_dang_ky]
GO
ALTER TABLE [dbo].[khach_hang]  WITH CHECK ADD  CONSTRAINT [FK_khachhang_taikhoan] FOREIGN KEY([ma_tai_khoan])
REFERENCES [dbo].[tai_khoan] ([ma_tai_khoan])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[khach_hang] CHECK CONSTRAINT [FK_khachhang_taikhoan]
GO

-- Bảng: dia_chi_giao_hang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[dia_chi_giao_hang](
	[ma_dia_chi] [int] IDENTITY(1,1) NOT NULL,
	[makh] [int] NOT NULL,
	[ten_nguoi_nhan] [nvarchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[so_dien_thoai] [varchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[dia_chi_chi_tiet] [nvarchar](255) COLLATE Vietnamese_CI_AS NOT NULL,
	[phuong_xa] [nvarchar](100) COLLATE Vietnamese_CI_AS NULL,
	[quan_huyen] [nvarchar](100) COLLATE Vietnamese_CI_AS NULL,
	[tinh_thanh] [nvarchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[la_mac_dinh] [bit] NOT NULL,
	[ngay_tao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_dia_chi] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[dia_chi_giao_hang] ADD  CONSTRAINT [DF_diachi_lamacdinh]  DEFAULT ((0)) FOR [la_mac_dinh]
GO
ALTER TABLE [dbo].[dia_chi_giao_hang] ADD  CONSTRAINT [DF_diachi_ngaytao]  DEFAULT (sysutcdatetime()) FOR [ngay_tao]
GO
ALTER TABLE [dbo].[dia_chi_giao_hang]  WITH CHECK ADD  CONSTRAINT [FK_diachi_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[dia_chi_giao_hang] CHECK CONSTRAINT [FK_diachi_khachhang]
GO

-- Bảng: shop
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[shop](
	[ma_shop] [int] IDENTITY(1,1) NOT NULL,
	[ma_tai_khoan] [int] NOT NULL,
	[ten_shop] [nvarchar](150) COLLATE Vietnamese_CI_AS NOT NULL,
	[slug] [varchar](150) COLLATE Vietnamese_CI_AS NOT NULL,
	[logo] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[banner] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[mo_ta] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
	[dia_chi_shop] [nvarchar](255) COLLATE Vietnamese_CI_AS NULL,
	[so_dien_thoai] [varchar](20) COLLATE Vietnamese_CI_AS NULL,
	[email_shop] [varchar](100) COLLATE Vietnamese_CI_AS NULL,
	[chiet_khau_phan_tram] [decimal](5, 2) NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ngay_tao] [datetime2](7) NOT NULL,
	[ngay_cap_nhat] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_shop] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[ma_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [UQ_shop_slug] UNIQUE NONCLUSTERED 
(
	[slug] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[shop] ADD  CONSTRAINT [DF_shop_chietkhau]  DEFAULT ((5.00)) FOR [chiet_khau_phan_tram]
GO
ALTER TABLE [dbo].[shop] ADD  CONSTRAINT [DF_shop_trangthai]  DEFAULT (N'ChoDuyet') FOR [trang_thai]
GO
ALTER TABLE [dbo].[shop] ADD  CONSTRAINT [DF_shop_ngaytao]  DEFAULT (sysutcdatetime()) FOR [ngay_tao]
GO
ALTER TABLE [dbo].[shop]  WITH CHECK ADD  CONSTRAINT [FK_shop_taikhoan] FOREIGN KEY([ma_tai_khoan])
REFERENCES [dbo].[tai_khoan] ([ma_tai_khoan])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[shop] CHECK CONSTRAINT [FK_shop_taikhoan]
GO
ALTER TABLE [dbo].[shop]  WITH CHECK ADD  CONSTRAINT [CK_shop_trangthai] CHECK  (([trang_thai]=N'BiKhoa' OR [trang_thai]=N'HoatDong' OR [trang_thai]=N'ChoDuyet'))
GO
ALTER TABLE [dbo].[shop] CHECK CONSTRAINT [CK_shop_trangthai]
GO

-- Bảng: danh_muc
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[danh_muc](
	[ma_danh_muc] [int] IDENTITY(1,1) NOT NULL,
	[ten_danh_muc] [nvarchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[mo_ta] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[trang_thai] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_danh_muc] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[danh_muc] ADD  CONSTRAINT [DF_danhmuc_trangthai]  DEFAULT ((1)) FOR [trang_thai]
GO

-- Bảng: tac_gia
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tac_gia](
	[ma_tac_gia] [int] IDENTITY(1,1) NOT NULL,
	[ten_tac_gia] [nvarchar](150) COLLATE Vietnamese_CI_AS NOT NULL,
	[mo_ta] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_tac_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

-- Bảng: nha_xuat_ban
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[nha_xuat_ban](
	[manxb] [int] IDENTITY(1,1) NOT NULL,
	[tennxb] [nvarchar](150) COLLATE Vietnamese_CI_AS NOT NULL,
	[dia_chi] [nvarchar](255) COLLATE Vietnamese_CI_AS NULL,
	[so_dien_thoai] [varchar](20) COLLATE Vietnamese_CI_AS NULL,
	[email] [varchar](100) COLLATE Vietnamese_CI_AS NULL,
PRIMARY KEY CLUSTERED 
(
	[manxb] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO

-- Bảng: san_pham
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[san_pham](
	[masp] [int] IDENTITY(1,1) NOT NULL,
	[ma_shop] [int] NOT NULL,
	[ma_danh_muc] [int] NOT NULL,
	[tensp] [nvarchar](200) COLLATE Vietnamese_CI_AS NOT NULL,
	[ten_nha_xuat_ban] [nvarchar](150) COLLATE Vietnamese_CI_AS NULL,
	[isbn] [varchar](20) COLLATE Vietnamese_CI_AS NULL,
	[gia_ban] [int] NOT NULL,
	[so_luong_ton] [int] NOT NULL,
	[so_luong_da_ban] [int] NOT NULL,
	[muc_ton_toi_thieu] [int] NOT NULL,
	[loaisp] [nvarchar](50) COLLATE Vietnamese_CI_AS NOT NULL,
	[mo_ta] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
	[hinh_anh] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[trang_thai_khoa] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ngay_tao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
CREATE NONCLUSTERED INDEX [idx_sanpham_giaban] ON [dbo].[san_pham]
(
	[gia_ban] ASC
)
INCLUDE([tensp],[hinh_anh],[ma_danh_muc],[ma_shop]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
SET ANSI_PADDING ON

GO
CREATE NONCLUSTERED INDEX [idx_sanpham_shop_trangthai] ON [dbo].[san_pham]
(
	[ma_shop] ASC,
	[trang_thai] ASC
)
INCLUDE([tensp],[gia_ban],[so_luong_ton]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_tonkho]  DEFAULT ((0)) FOR [so_luong_ton]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_daban]  DEFAULT ((0)) FOR [so_luong_da_ban]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_tontoithieu]  DEFAULT ((5)) FOR [muc_ton_toi_thieu]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_loai]  DEFAULT (N'SACH') FOR [loaisp]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_trangthai]  DEFAULT (N'HoatDong') FOR [trang_thai]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_khoa]  DEFAULT (N'BinhThuong') FOR [trang_thai_khoa]
GO
ALTER TABLE [dbo].[san_pham] ADD  CONSTRAINT [DF_sanpham_ngaytao]  DEFAULT (sysutcdatetime()) FOR [ngay_tao]
GO
ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [FK_sanpham_danhmuc] FOREIGN KEY([ma_danh_muc])
REFERENCES [dbo].[danh_muc] ([ma_danh_muc])
GO
ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [FK_sanpham_danhmuc]
GO
ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [FK_sanpham_shop] FOREIGN KEY([ma_shop])
REFERENCES [dbo].[shop] ([ma_shop])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [FK_sanpham_shop]
GO
ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [CK_sanpham_giaban] CHECK  (([gia_ban]>=(0)))
GO
ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [CK_sanpham_giaban]
GO
ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [CK_sanpham_tonkho] CHECK  (([so_luong_ton]>=(0)))
GO
ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [CK_sanpham_tonkho]
GO

-- Bảng: san_pham_tac_gia
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[san_pham_tac_gia](
	[masp] [int] NOT NULL,
	[ma_tac_gia] [int] NOT NULL,
	[thu_tu_tac_gia] [int] NULL,
 CONSTRAINT [PK_san_pham_tac_gia] PRIMARY KEY CLUSTERED 
(
	[masp] ASC,
	[ma_tac_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[san_pham_tac_gia]  WITH CHECK ADD  CONSTRAINT [FK_sptg_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[san_pham_tac_gia] CHECK CONSTRAINT [FK_sptg_sanpham]
GO
ALTER TABLE [dbo].[san_pham_tac_gia]  WITH CHECK ADD  CONSTRAINT [FK_sptg_tacgia] FOREIGN KEY([ma_tac_gia])
REFERENCES [dbo].[tac_gia] ([ma_tac_gia])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[san_pham_tac_gia] CHECK CONSTRAINT [FK_sptg_tacgia]
GO

-- Bảng: phieu_kiem_ke
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phieu_kiem_ke](
	[ma_phieu_kiem_ke] [int] IDENTITY(1,1) NOT NULL,
	[ma_shop] [int] NOT NULL,
	[ngay_kiem_ke] [datetime2](7) NOT NULL,
	[ngay_phe_duyet] [datetime2](7) NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ghi_chu] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_phieu_kiem_ke] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[phieu_kiem_ke] ADD  CONSTRAINT [DF_kiemke_ngay]  DEFAULT (sysutcdatetime()) FOR [ngay_kiem_ke]
GO
ALTER TABLE [dbo].[phieu_kiem_ke] ADD  CONSTRAINT [DF_kiemke_trangthai]  DEFAULT (N'DangKiemKe') FOR [trang_thai]
GO
ALTER TABLE [dbo].[phieu_kiem_ke]  WITH CHECK ADD  CONSTRAINT [FK_kiemke_shop] FOREIGN KEY([ma_shop])
REFERENCES [dbo].[shop] ([ma_shop])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[phieu_kiem_ke] CHECK CONSTRAINT [FK_kiemke_shop]
GO
ALTER TABLE [dbo].[phieu_kiem_ke]  WITH CHECK ADD  CONSTRAINT [CK_kiemke_trangthai] CHECK  (([trang_thai]=N'DaHuy' OR [trang_thai]=N'DaCanBang' OR [trang_thai]=N'DangKiemKe'))
GO
ALTER TABLE [dbo].[phieu_kiem_ke] CHECK CONSTRAINT [CK_kiemke_trangthai]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Trigger 3: Cân bằng tồn kho khi Shop duyệt phiếu kiểm kê
CREATE   TRIGGER trg_PhieuKiemKe_CanBangTonKho
ON phieu_kiem_ke
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Khi trạng thái chuyển sang 'DaCanBang'
    IF UPDATE(trang_thai)
    BEGIN
        UPDATE sp
        SET sp.so_luong_ton = ctkk.so_luong_thuc_te
        FROM san_pham sp
        JOIN chi_tiet_kiem_ke ctkk ON sp.masp = ctkk.masp
        JOIN inserted i ON ctkk.ma_phieu_kiem_ke = i.ma_phieu_kiem_ke
        JOIN deleted d ON i.ma_phieu_kiem_ke = d.ma_phieu_kiem_ke
        WHERE i.trang_thai = N'DaCanBang' AND d.trang_thai != N'DaCanBang';

        -- Cáº­p nháº­t ngày phê duyệt
        UPDATE pkk
        SET pkk.ngay_phe_duyet = SYSUTCDATETIME()
        FROM phieu_kiem_ke pkk
        JOIN inserted i ON pkk.ma_phieu_kiem_ke = i.ma_phieu_kiem_ke
        JOIN deleted d ON i.ma_phieu_kiem_ke = d.ma_phieu_kiem_ke
        WHERE i.trang_thai = N'DaCanBang' AND d.trang_thai != N'DaCanBang' AND pkk.ngay_phe_duyet IS NULL;
    END
END;

GO
ALTER TABLE [dbo].[phieu_kiem_ke] ENABLE TRIGGER [trg_PhieuKiemKe_CanBangTonKho]
GO

-- Bảng: chi_tiet_kiem_ke
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_kiem_ke](
	[ma_phieu_kiem_ke] [int] NOT NULL,
	[masp] [int] NOT NULL,
	[so_luong_he_thong] [int] NOT NULL,
	[so_luong_thuc_te] [int] NOT NULL,
	[chenh_lech] [int] NOT NULL,
	[ly_do] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
 CONSTRAINT [PK_chi_tiet_kiem_ke] PRIMARY KEY CLUSTERED 
(
	[ma_phieu_kiem_ke] ASC,
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke] ADD  CONSTRAINT [DF_ctkk_chenhlech]  DEFAULT ((0)) FOR [chenh_lech]
GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke]  WITH CHECK ADD  CONSTRAINT [FK_ctkk_phieu] FOREIGN KEY([ma_phieu_kiem_ke])
REFERENCES [dbo].[phieu_kiem_ke] ([ma_phieu_kiem_ke])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke] CHECK CONSTRAINT [FK_ctkk_phieu]
GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke]  WITH CHECK ADD  CONSTRAINT [FK_ctkk_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke] CHECK CONSTRAINT [FK_ctkk_sanpham]
GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke]  WITH CHECK ADD  CONSTRAINT [CK_ctkk_soluongthucte] CHECK  (([so_luong_thuc_te]>=(0)))
GO
ALTER TABLE [dbo].[chi_tiet_kiem_ke] CHECK CONSTRAINT [CK_ctkk_soluongthucte]
GO

-- Bảng: nha_van_chuyen
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[nha_van_chuyen](
	[ma_nvc] [int] IDENTITY(1,1) NOT NULL,
	[ten_nvc] [nvarchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[phi_co_ban] [int] NOT NULL,
	[thoi_gian_du_kien] [nvarchar](50) COLLATE Vietnamese_CI_AS NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_nvc] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[nha_van_chuyen] ADD  CONSTRAINT [DF_nvc_phi]  DEFAULT ((25000)) FOR [phi_co_ban]
GO
ALTER TABLE [dbo].[nha_van_chuyen] ADD  CONSTRAINT [DF_nvc_tg]  DEFAULT (N'2 - 3 ngày') FOR [thoi_gian_du_kien]
GO
ALTER TABLE [dbo].[nha_van_chuyen] ADD  CONSTRAINT [DF_nvc_trangthai]  DEFAULT (N'HoatDong') FOR [trang_thai]
GO

-- Bảng: khuyen_mai
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[khuyen_mai](
	[makm] [int] IDENTITY(1,1) NOT NULL,
	[ma_code] [varchar](50) COLLATE Vietnamese_CI_AS NOT NULL,
	[tenkm] [nvarchar](150) COLLATE Vietnamese_CI_AS NOT NULL,
	[mo_ta] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[loai_giam] [nvarchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[gia_tri_giam] [int] NOT NULL,
	[giam_toi_da] [int] NULL,
	[don_toi_thieu] [int] NOT NULL,
	[so_luong_toi_da] [int] NULL,
	[so_luong_da_dung] [int] NOT NULL,
	[pham_vi] [varchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[ma_shop] [int] NULL,
	[ngay_bat_dau] [datetime2](7) NOT NULL,
	[ngay_ket_thuc] [datetime2](7) NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[hien_thi_cong_khai] [bit] NOT NULL,
	[gioi_han_moi_khach_hang] [int] NOT NULL,
	[doi_tuong_khach_hang] [nvarchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[loai_khuyen_mai] [varchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[makm] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [UQ_khuyenmai_code] UNIQUE NONCLUSTERED 
(
	[ma_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
SET ANSI_PADDING ON

GO
CREATE NONCLUSTERED INDEX [idx_khuyenmai_code] ON [dbo].[khuyen_mai]
(
	[ma_code] ASC,
	[trang_thai] ASC
)
INCLUDE([loai_giam],[gia_tri_giam],[don_toi_thieu],[pham_vi],[ma_shop],[ngay_bat_dau],[ngay_ket_thuc]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_dontoithieu]  DEFAULT ((0)) FOR [don_toi_thieu]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_dadung]  DEFAULT ((0)) FOR [so_luong_da_dung]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_phamvi]  DEFAULT ('TOAN_SAN') FOR [pham_vi]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_trangthai]  DEFAULT (N'HoatDong') FOR [trang_thai]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_congbokhuyenmai]  DEFAULT ((1)) FOR [hien_thi_cong_khai]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_gioihan]  DEFAULT ((1)) FOR [gioi_han_moi_khach_hang]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_doituong]  DEFAULT ('ALL') FOR [doi_tuong_khach_hang]
GO
ALTER TABLE [dbo].[khuyen_mai] ADD  CONSTRAINT [DF_km_loaikm]  DEFAULT ('GIAM_GIA_SAN_PHAM') FOR [loai_khuyen_mai]
GO
ALTER TABLE [dbo].[khuyen_mai]  WITH CHECK ADD  CONSTRAINT [FK_km_shop] FOREIGN KEY([ma_shop])
REFERENCES [dbo].[shop] ([ma_shop])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[khuyen_mai] CHECK CONSTRAINT [FK_km_shop]
GO
ALTER TABLE [dbo].[khuyen_mai]  WITH CHECK ADD  CONSTRAINT [CK_km_giatri] CHECK  (([gia_tri_giam]>(0)))
GO
ALTER TABLE [dbo].[khuyen_mai] CHECK CONSTRAINT [CK_km_giatri]
GO
ALTER TABLE [dbo].[khuyen_mai]  WITH CHECK ADD  CONSTRAINT [CK_km_loaigiam] CHECK  (([loai_giam]=N'Freeship' OR [loai_giam]=N'TienCoDinh' OR [loai_giam]=N'PhanTram'))
GO
ALTER TABLE [dbo].[khuyen_mai] CHECK CONSTRAINT [CK_km_loaigiam]
GO
ALTER TABLE [dbo].[khuyen_mai]  WITH CHECK ADD  CONSTRAINT [CK_km_ngay] CHECK  (([ngay_ket_thuc]>=[ngay_bat_dau]))
GO
ALTER TABLE [dbo].[khuyen_mai] CHECK CONSTRAINT [CK_km_ngay]
GO
ALTER TABLE [dbo].[khuyen_mai]  WITH CHECK ADD  CONSTRAINT [CK_km_phamvi] CHECK  (([pham_vi]='SHOP' OR [pham_vi]='TOAN_SAN'))
GO
ALTER TABLE [dbo].[khuyen_mai] CHECK CONSTRAINT [CK_km_phamvi]
GO

-- Bảng: voucher_da_luu
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[voucher_da_luu](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[makh] [int] NOT NULL,
	[makm] [int] NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ngay_luu] [datetime2](7) NOT NULL,
	[ngay_su_dung] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [UQ_voucherdaluu] UNIQUE NONCLUSTERED 
(
	[makh] ASC,
	[makm] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[voucher_da_luu] ADD  CONSTRAINT [DF_voucherdaluu_trangthai]  DEFAULT (N'CHUA_DUNG') FOR [trang_thai]
GO
ALTER TABLE [dbo].[voucher_da_luu] ADD  CONSTRAINT [DF_voucherdaluu_ngayluu]  DEFAULT (sysutcdatetime()) FOR [ngay_luu]
GO
ALTER TABLE [dbo].[voucher_da_luu]  WITH CHECK ADD  CONSTRAINT [FK_voucherdaluu_km] FOREIGN KEY([makm])
REFERENCES [dbo].[khuyen_mai] ([makm])
GO
ALTER TABLE [dbo].[voucher_da_luu] CHECK CONSTRAINT [FK_voucherdaluu_km]
GO
ALTER TABLE [dbo].[voucher_da_luu]  WITH CHECK ADD  CONSTRAINT [FK_voucherdaluu_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[voucher_da_luu] CHECK CONSTRAINT [FK_voucherdaluu_khachhang]
GO

-- Bảng: gio_hang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[gio_hang](
	[ma_gio_hang] [int] IDENTITY(1,1) NOT NULL,
	[makh] [int] NOT NULL,
	[ngay_tao] [datetime2](7) NOT NULL,
	[ngay_cap_nhat] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_gio_hang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[makh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[gio_hang] ADD  CONSTRAINT [DF_giohang_ngaytao]  DEFAULT (sysutcdatetime()) FOR [ngay_tao]
GO
ALTER TABLE [dbo].[gio_hang] ADD  CONSTRAINT [DF_giohang_ngaycapnhat]  DEFAULT (sysutcdatetime()) FOR [ngay_cap_nhat]
GO
ALTER TABLE [dbo].[gio_hang]  WITH CHECK ADD  CONSTRAINT [FK_giohang_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[gio_hang] CHECK CONSTRAINT [FK_giohang_khachhang]
GO

-- Bảng: chi_tiet_gio_hang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_gio_hang](
	[ma_gio_hang] [int] NOT NULL,
	[masp] [int] NOT NULL,
	[so_luong] [int] NOT NULL,
 CONSTRAINT [PK_chi_tiet_gio_hang] PRIMARY KEY CLUSTERED 
(
	[ma_gio_hang] ASC,
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[chi_tiet_gio_hang]  WITH CHECK ADD  CONSTRAINT [FK_ctgh_giohang] FOREIGN KEY([ma_gio_hang])
REFERENCES [dbo].[gio_hang] ([ma_gio_hang])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[chi_tiet_gio_hang] CHECK CONSTRAINT [FK_ctgh_giohang]
GO
ALTER TABLE [dbo].[chi_tiet_gio_hang]  WITH CHECK ADD  CONSTRAINT [FK_ctgh_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
GO
ALTER TABLE [dbo].[chi_tiet_gio_hang] CHECK CONSTRAINT [FK_ctgh_sanpham]
GO
ALTER TABLE [dbo].[chi_tiet_gio_hang]  WITH CHECK ADD  CONSTRAINT [CK_ctgh_soluong] CHECK  (([so_luong]>(0)))
GO
ALTER TABLE [dbo].[chi_tiet_gio_hang] CHECK CONSTRAINT [CK_ctgh_soluong]
GO

-- Bảng: don_hang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[don_hang](
	[madh] [int] IDENTITY(1,1) NOT NULL,
	[makh] [int] NOT NULL,
	[ma_shop] [int] NOT NULL,
	[ma_nvc] [int] NULL,
	[makm] [int] NULL,
	[dia_chi_giao] [nvarchar](255) COLLATE Vietnamese_CI_AS NOT NULL,
	[so_dien_thoai_giao] [varchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[tong_tien] [int] NOT NULL,
	[phi_van_chuyen] [int] NOT NULL,
	[tien_giam] [int] NOT NULL,
	[chiet_khau_app_phan_tram] [decimal](5, 2) NOT NULL,
	[tien_phi_san] [int] NOT NULL,
	[tien_thuc_nhan_shop] [int] NOT NULL,
	[phuong_thuc_thanh_toan] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[trang_thai_thanh_toan] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ma_van_don] [varchar](50) COLLATE Vietnamese_CI_AS NULL,
	[ly_do_huy] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[ly_do_tu_choi] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[ngay_dat] [datetime2](7) NOT NULL,
	[ngay_xac_nhan] [datetime2](7) NULL,
	[ngay_hoan_thanh] [datetime2](7) NULL,
	[ly_do_tra_hang] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
	[phan_quyet_tranh_chap] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
	[nguoi_xu_ly_tranh_chap] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[madh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
CREATE NONCLUSTERED INDEX [idx_donhang_makh_ngaydat] ON [dbo].[don_hang]
(
	[makh] ASC,
	[ngay_dat] DESC
)
INCLUDE([tong_tien],[trang_thai],[ma_shop]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
SET ANSI_PADDING ON

GO
CREATE NONCLUSTERED INDEX [idx_donhang_shop_trangthai] ON [dbo].[don_hang]
(
	[ma_shop] ASC,
	[trang_thai] ASC
)
INCLUDE([tong_tien],[ngay_dat],[makh]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_phiship]  DEFAULT ((0)) FOR [phi_van_chuyen]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_tiengiam]  DEFAULT ((0)) FOR [tien_giam]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_chietkhau]  DEFAULT ((5.00)) FOR [chiet_khau_app_phan_tram]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_phisan]  DEFAULT ((0)) FOR [tien_phi_san]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_shopnhan]  DEFAULT ((0)) FOR [tien_thuc_nhan_shop]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_pttt]  DEFAULT (N'COD') FOR [phuong_thuc_thanh_toan]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_tttt]  DEFAULT (N'ChuaThanhToan') FOR [trang_thai_thanh_toan]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_trangthai]  DEFAULT (N'ChoXacNhan') FOR [trang_thai]
GO
ALTER TABLE [dbo].[don_hang] ADD  CONSTRAINT [DF_donhang_ngaydat]  DEFAULT (sysutcdatetime()) FOR [ngay_dat]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [FK_donhang_km] FOREIGN KEY([makm])
REFERENCES [dbo].[khuyen_mai] ([makm])
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [FK_donhang_km]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [FK_donhang_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [FK_donhang_khachhang]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [FK_donhang_nvc] FOREIGN KEY([ma_nvc])
REFERENCES [dbo].[nha_van_chuyen] ([ma_nvc])
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [FK_donhang_nvc]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [FK_donhang_nguoixuly] FOREIGN KEY([nguoi_xu_ly_tranh_chap])
REFERENCES [dbo].[tai_khoan] ([ma_tai_khoan])
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [FK_donhang_nguoixuly]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [FK_donhang_shop] FOREIGN KEY([ma_shop])
REFERENCES [dbo].[shop] ([ma_shop])
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [FK_donhang_shop]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [CK_donhang_chietkhau] CHECK  (([chiet_khau_app_phan_tram]>=(0) AND [chiet_khau_app_phan_tram]<=(100)))
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [CK_donhang_chietkhau]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [CK_donhang_tongtien] CHECK  (([tong_tien]>=(0)))
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [CK_donhang_tongtien]
GO
ALTER TABLE [dbo].[don_hang]  WITH CHECK ADD  CONSTRAINT [CK_donhang_trangthai] CHECK  (([trang_thai]=N'DaHoan' OR [trang_thai]=N'TranhChap' OR [trang_thai]=N'TraHangHoanTien' OR [trang_thai]=N'TraHang' OR [trang_thai]=N'Huy' OR [trang_thai]=N'DaHuy' OR [trang_thai]=N'YeuCauHuy' OR [trang_thai]=N'HoanTat' OR [trang_thai]=N'GiaoThanhCong' OR [trang_thai]=N'DaGiao' OR [trang_thai]=N'DangGiao' OR [trang_thai]=N'DaLayHang' OR [trang_thai]=N'DaXacNhan' OR [trang_thai]=N'ChoXacNhan' OR [trang_thai]=N'DonHangMoi' OR [trang_thai]=N'ChoDuyet' OR [trang_thai]=N'ChoXuLy' OR [trang_thai]=N'ChoThanhToan'))
GO
ALTER TABLE [dbo].[don_hang] CHECK CONSTRAINT [CK_donhang_trangthai]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Trigger 2: Tự động hoàn lại tồn kho khi đơn hàng bị HỦY
CREATE   TRIGGER trg_DonHang_HoanTonKhoKhiHuy
ON don_hang
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ kÃ­ch hoáº¡t khi đơn hàng chuyển từ trạng thái khác sang 'DaHuy'
    IF UPDATE(trang_thai)
    BEGIN
        UPDATE sp
        SET sp.so_luong_ton = sp.so_luong_ton + ctdh.so_luong,
            sp.so_luong_da_ban = CASE WHEN sp.so_luong_da_ban >= ctdh.so_luong THEN sp.so_luong_da_ban - ctdh.so_luong ELSE 0 END
        FROM san_pham sp
        JOIN chi_tiet_don_hang ctdh ON sp.masp = ctdh.masp
        JOIN inserted i ON ctdh.madh = i.madh
        JOIN deleted d ON i.madh = d.madh
        WHERE i.trang_thai = N'DaHuy' AND d.trang_thai != N'DaHuy';
    END
END;

GO
ALTER TABLE [dbo].[don_hang] ENABLE TRIGGER [trg_DonHang_HoanTonKhoKhiHuy]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Trigger 4: Tự động tÃ­nh tiá»n phí­ sàn vÃ  tiá»n Shop thá»±c nháº­n
CREATE   TRIGGER trg_DonHang_TinhTienPhiSanVaShopNhan
ON don_hang
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Tránh vòng lặp lồng nhau
    IF TRIGGER_NESTLEVEL() > 1 RETURN;

    UPDATE dh
    SET dh.tien_phi_san = CAST(ROUND(dh.tong_tien * (dh.chiet_khau_app_phan_tram / 100.0), 0) AS INT),
        dh.tien_thuc_nhan_shop = dh.tong_tien - CAST(ROUND(dh.tong_tien * (dh.chiet_khau_app_phan_tram / 100.0), 0) AS INT)
    FROM don_hang dh
    JOIN inserted i ON dh.madh = i.madh;
END;

GO
ALTER TABLE [dbo].[don_hang] ENABLE TRIGGER [trg_DonHang_TinhTienPhiSanVaShopNhan]
GO

-- Bảng: chi_tiet_don_hang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_don_hang](
	[madh] [int] NOT NULL,
	[masp] [int] NOT NULL,
	[don_gia] [int] NOT NULL,
	[so_luong] [int] NOT NULL,
 CONSTRAINT [PK_chi_tiet_don_hang] PRIMARY KEY CLUSTERED 
(
	[madh] ASC,
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[chi_tiet_don_hang]  WITH CHECK ADD  CONSTRAINT [FK_ctdh_donhang] FOREIGN KEY([madh])
REFERENCES [dbo].[don_hang] ([madh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[chi_tiet_don_hang] CHECK CONSTRAINT [FK_ctdh_donhang]
GO
ALTER TABLE [dbo].[chi_tiet_don_hang]  WITH CHECK ADD  CONSTRAINT [FK_ctdh_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
GO
ALTER TABLE [dbo].[chi_tiet_don_hang] CHECK CONSTRAINT [FK_ctdh_sanpham]
GO
ALTER TABLE [dbo].[chi_tiet_don_hang]  WITH CHECK ADD  CONSTRAINT [CK_ctdh_dongia] CHECK  (([don_gia]>=(0)))
GO
ALTER TABLE [dbo].[chi_tiet_don_hang] CHECK CONSTRAINT [CK_ctdh_dongia]
GO
ALTER TABLE [dbo].[chi_tiet_don_hang]  WITH CHECK ADD  CONSTRAINT [CK_ctdh_soluong] CHECK  (([so_luong]>(0)))
GO
ALTER TABLE [dbo].[chi_tiet_don_hang] CHECK CONSTRAINT [CK_ctdh_soluong]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- ====================================================================
-- PHẦN 2: HÀM (FUNCTIONS)
-- ====================================================================

-- Trigger 1: Tự động kiá»ƒm tra vÃ  trừ tồn kho sách của Shop khi đặt hÃ ng
CREATE   TRIGGER trg_ChiTietDonHang_TruTonKho
ON chi_tiet_don_hang
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra nếu số lượng tá»“n không đủ
    IF EXISTS (
        SELECT 1 
        FROM inserted i
        JOIN san_pham sp ON i.masp = sp.masp
        WHERE sp.so_luong_ton < i.so_luong
    )
    BEGIN
        RAISERROR (N'Lỗi: Sá»‘ lÆ°á»£ng sách trong kho của Shop không đủ để đáp ứng đơn hàng!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Cáº­p nháº­t trừ số lượng tá»“n vÃ  tăng số lượng đã bán
    UPDATE sp
    SET sp.so_luong_ton = sp.so_luong_ton - i.so_luong,
        sp.so_luong_da_ban = sp.so_luong_da_ban + i.so_luong
    FROM san_pham sp
    JOIN inserted i ON sp.masp = i.masp;
END;

GO
ALTER TABLE [dbo].[chi_tiet_don_hang] ENABLE TRIGGER [trg_ChiTietDonHang_TruTonKho]
GO

-- Bảng: thanh_toan
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[thanh_toan](
	[ma_thanh_toan] [int] IDENTITY(1,1) NOT NULL,
	[madh] [int] NOT NULL,
	[ma_giao_dich] [varchar](100) COLLATE Vietnamese_CI_AS NULL,
	[phuong_thuc] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[so_tien] [int] NOT NULL,
	[noi_dung] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ngay_thanh_toan] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_thanh_toan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[thanh_toan] ADD  CONSTRAINT [DF_thanhtoan_trangthai]  DEFAULT (N'ChoThanhToan') FOR [trang_thai]
GO
ALTER TABLE [dbo].[thanh_toan] ADD  CONSTRAINT [DF_thanhtoan_ngay]  DEFAULT (sysutcdatetime()) FOR [ngay_thanh_toan]
GO
ALTER TABLE [dbo].[thanh_toan]  WITH CHECK ADD  CONSTRAINT [FK_thanhtoan_donhang] FOREIGN KEY([madh])
REFERENCES [dbo].[don_hang] ([madh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[thanh_toan] CHECK CONSTRAINT [FK_thanhtoan_donhang]
GO
ALTER TABLE [dbo].[thanh_toan]  WITH CHECK ADD  CONSTRAINT [CK_thanhtoan_sotien] CHECK  (([so_tien]>=(0)))
GO
ALTER TABLE [dbo].[thanh_toan] CHECK CONSTRAINT [CK_thanhtoan_sotien]
GO

-- Bảng: hoa_don
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hoa_don](
	[ma_hd] [int] IDENTITY(1,1) NOT NULL,
	[ma_dh] [int] NOT NULL,
	[ngay_lap] [datetime2](7) NOT NULL,
	[tong_tien_thanh_toan] [int] NOT NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_hd] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[ma_dh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoadon_ngay]  DEFAULT (sysutcdatetime()) FOR [ngay_lap]
GO
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoadon_trangthai]  DEFAULT (N'ChuaThanhToan') FOR [trang_thai]
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [FK_hoadon_donhang] FOREIGN KEY([ma_dh])
REFERENCES [dbo].[don_hang] ([madh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [FK_hoadon_donhang]
GO

-- Bảng: danh_gia
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[danh_gia](
	[ma_danh_gia] [int] IDENTITY(1,1) NOT NULL,
	[madh] [int] NOT NULL,
	[makh] [int] NOT NULL,
	[masp] [int] NOT NULL,
	[so_sao] [int] NOT NULL,
	[noi_dung] [nvarchar](1000) COLLATE Vietnamese_CI_AS NULL,
	[hinh_anh_danh_gia] [nvarchar](500) COLLATE Vietnamese_CI_AS NULL,
	[trang_thai] [nvarchar](30) COLLATE Vietnamese_CI_AS NOT NULL,
	[ngay_danh_gia] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_danh_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [UQ_danhgia_makh_masp_madh] UNIQUE NONCLUSTERED 
(
	[madh] ASC,
	[makh] ASC,
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[danh_gia] ADD  CONSTRAINT [DF_danhgia_trangthai]  DEFAULT (N'HienThi') FOR [trang_thai]
GO
ALTER TABLE [dbo].[danh_gia] ADD  CONSTRAINT [DF_danhgia_ngay]  DEFAULT (sysutcdatetime()) FOR [ngay_danh_gia]
GO
ALTER TABLE [dbo].[danh_gia]  WITH CHECK ADD  CONSTRAINT [FK_danhgia_donhang] FOREIGN KEY([madh])
REFERENCES [dbo].[don_hang] ([madh])
GO
ALTER TABLE [dbo].[danh_gia] CHECK CONSTRAINT [FK_danhgia_donhang]
GO
ALTER TABLE [dbo].[danh_gia]  WITH CHECK ADD  CONSTRAINT [FK_danhgia_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
GO
ALTER TABLE [dbo].[danh_gia] CHECK CONSTRAINT [FK_danhgia_khachhang]
GO
ALTER TABLE [dbo].[danh_gia]  WITH CHECK ADD  CONSTRAINT [FK_danhgia_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
GO
ALTER TABLE [dbo].[danh_gia] CHECK CONSTRAINT [FK_danhgia_sanpham]
GO
ALTER TABLE [dbo].[danh_gia]  WITH CHECK ADD  CONSTRAINT [CK_danhgia_sosao] CHECK  (([so_sao]>=(1) AND [so_sao]<=(5)))
GO
ALTER TABLE [dbo].[danh_gia] CHECK CONSTRAINT [CK_danhgia_sosao]
GO

-- Bảng: danh_gia_media
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[danh_gia_media](
	[ma_media] [int] IDENTITY(1,1) NOT NULL,
	[ma_danh_gia] [int] NOT NULL,
	[loai_media] [varchar](10) COLLATE Vietnamese_CI_AS NOT NULL,
	[url] [nvarchar](500) COLLATE Vietnamese_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_media] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[danh_gia_media] ADD  CONSTRAINT [DF_dgmedia_loai]  DEFAULT ('IMAGE') FOR [loai_media]
GO
ALTER TABLE [dbo].[danh_gia_media]  WITH CHECK ADD  CONSTRAINT [FK_dgmedia_danhgia] FOREIGN KEY([ma_danh_gia])
REFERENCES [dbo].[danh_gia] ([ma_danh_gia])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[danh_gia_media] CHECK CONSTRAINT [FK_dgmedia_danhgia]
GO

-- Bảng: san_pham_yeu_thich
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[san_pham_yeu_thich](
	[makh] [int] NOT NULL,
	[masp] [int] NOT NULL,
	[ngay_thich] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_san_pham_yeu_thich] PRIMARY KEY CLUSTERED 
(
	[makh] ASC,
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[san_pham_yeu_thich] ADD  CONSTRAINT [DF_spyt_ngay]  DEFAULT (sysutcdatetime()) FOR [ngay_thich]
GO
ALTER TABLE [dbo].[san_pham_yeu_thich]  WITH CHECK ADD  CONSTRAINT [FK_spyt_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[san_pham_yeu_thich] CHECK CONSTRAINT [FK_spyt_khachhang]
GO
ALTER TABLE [dbo].[san_pham_yeu_thich]  WITH CHECK ADD  CONSTRAINT [FK_spyt_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
GO
ALTER TABLE [dbo].[san_pham_yeu_thich] CHECK CONSTRAINT [FK_spyt_sanpham]
GO

-- Bảng: san_pham_da_xem
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[san_pham_da_xem](
	[makh] [int] NOT NULL,
	[masp] [int] NOT NULL,
	[thoi_gian_xem] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_san_pham_da_xem] PRIMARY KEY CLUSTERED 
(
	[makh] ASC,
	[masp] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[san_pham_da_xem] ADD  CONSTRAINT [DF_spdx_thoigian]  DEFAULT (sysutcdatetime()) FOR [thoi_gian_xem]
GO
ALTER TABLE [dbo].[san_pham_da_xem]  WITH CHECK ADD  CONSTRAINT [FK_spdx_khachhang] FOREIGN KEY([makh])
REFERENCES [dbo].[khach_hang] ([makh])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[san_pham_da_xem] CHECK CONSTRAINT [FK_spdx_khachhang]
GO
ALTER TABLE [dbo].[san_pham_da_xem]  WITH CHECK ADD  CONSTRAINT [FK_spdx_sanpham] FOREIGN KEY([masp])
REFERENCES [dbo].[san_pham] ([masp])
GO
ALTER TABLE [dbo].[san_pham_da_xem] CHECK CONSTRAINT [FK_spdx_sanpham]
GO

-- Bảng: password_reset_token
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[password_reset_token](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[otp_code] [varchar](10) COLLATE Vietnamese_CI_AS NOT NULL,
	[token] [varchar](100) COLLATE Vietnamese_CI_AS NULL,
	[ma_tai_khoan] [int] NOT NULL,
	[expiry_date] [datetime2](7) NOT NULL,
	[used] [bit] NOT NULL,
	[ngay_tao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[password_reset_token] ADD  CONSTRAINT [DF_prt_used]  DEFAULT ((0)) FOR [used]
GO
ALTER TABLE [dbo].[password_reset_token] ADD  CONSTRAINT [DF_prt_ngaytao]  DEFAULT (sysutcdatetime()) FOR [ngay_tao]
GO
ALTER TABLE [dbo].[password_reset_token]  WITH CHECK ADD  CONSTRAINT [FK_prt_taikhoan] FOREIGN KEY([ma_tai_khoan])
REFERENCES [dbo].[tai_khoan] ([ma_tai_khoan])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[password_reset_token] CHECK CONSTRAINT [FK_prt_taikhoan]
GO

-- Bảng: cau_hinh_he_thong
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[cau_hinh_he_thong](
	[ma_cau_hinh] [varchar](50) COLLATE Vietnamese_CI_AS NOT NULL,
	[gia_tri] [nvarchar](255) COLLATE Vietnamese_CI_AS NOT NULL,
	[mo_ta] [nvarchar](255) COLLATE Vietnamese_CI_AS NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_cau_hinh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO

-- Bảng: nhat_ky_bao_mat
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[nhat_ky_bao_mat](
	[ma_nhat_ky] [bigint] IDENTITY(1,1) NOT NULL,
	[ten_bang] [varchar](64) COLLATE Vietnamese_CI_AS NOT NULL,
	[hanh_dong] [varchar](20) COLLATE Vietnamese_CI_AS NOT NULL,
	[ban_ghi_id] [int] NULL,
	[du_lieu_cu] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
	[du_lieu_moi] [nvarchar](max) COLLATE Vietnamese_CI_AS NULL,
	[nguoi_thuc_hien] [nvarchar](100) COLLATE Vietnamese_CI_AS NOT NULL,
	[thoi_gian] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ma_nhat_ky] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[nhat_ky_bao_mat] ADD  CONSTRAINT [DF_audit_user]  DEFAULT (suser_sname()) FOR [nguoi_thuc_hien]
GO
ALTER TABLE [dbo].[nhat_ky_bao_mat] ADD  CONSTRAINT [DF_audit_time]  DEFAULT (sysutcdatetime()) FOR [thoi_gian]
GO

-- ====================================================================
-- PHẦN 2: HÀM (FUNCTIONS)
-- ====================================================================

-- Hàm: fn_BaoCaoDoanhThuTheoKhoangNgay
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- HÃ m 5 (Multi-Statement Table-Valued): Báo cáo doanh số theo khoảng thá»i gian
CREATE   FUNCTION fn_BaoCaoDoanhThuTheoKhoangNgay (
    @tu_ngay DATETIME2,
    @den_ngay DATETIME2
)
RETURNS @KetQua TABLE (
    ma_shop INT,
    ten_shop NVARCHAR(150),
    so_don INT,
    tong_tien_ban INT,
    phi_san INT,
    shop_nhan INT
)
AS
BEGIN
    INSERT INTO @KetQua
    SELECT 
        s.ma_shop,
        s.ten_shop,
        COUNT(dh.madh),
        ISNULL(SUM(dh.tong_tien), 0),
        ISNULL(SUM(dh.tien_phi_san), 0),
        ISNULL(SUM(dh.tien_thuc_nhan_shop), 0)
    FROM shop s
    JOIN don_hang dh ON s.ma_shop = dh.ma_shop
    WHERE dh.trang_thai IN (N'GiaoThanhCong', N'HoanTat')
      AND dh.ngay_dat BETWEEN @tu_ngay AND @den_ngay
    GROUP BY s.ma_shop, s.ten_shop;

    RETURN;
END;

GO

-- Hàm: fn_DemDonHang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

CREATE   FUNCTION dbo.fn_DemDonHang (@startDate DATETIME2, @endDate DATETIME2)
RETURNS BIGINT
AS
BEGIN
    DECLARE @dem BIGINT;
    SELECT @dem = COUNT(*)
    FROM don_hang
    WHERE ngay_dat >= @startDate AND ngay_dat <= @endDate;
    RETURN ISNULL(@dem, 0);
END;

GO

-- Hàm: fn_DemSachCuaShop
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- 2. Scalar Function: fn_DemSachCuaShop
CREATE   FUNCTION dbo.fn_DemSachCuaShop(@p_ma_shop INT)
RETURNS INT
AS
BEGIN
    DECLARE @v_count INT = 0;
    SELECT @v_count = COUNT(*)
    FROM san_pham
    WHERE ma_shop = @p_ma_shop
      AND (trang_thai_khoa IS NULL OR trang_thai_khoa != 'DaKhoa');
    RETURN @v_count;
END;

GO

-- Hàm: fn_KiemTraVoucherHopLe
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- HÃ m 3 (Scalar): Kiểm tra Voucher có há»£p lá»‡ cho đơn hàng hay không
CREATE   FUNCTION fn_KiemTraVoucherHopLe (
    @ma_code VARCHAR(50),
    @ma_shop INT,
    @tong_tien INT
)
RETURNS BIT
AS
BEGIN
    DECLARE @hop_le BIT = 0;
    DECLARE @now DATETIME2 = SYSUTCDATETIME();

    IF EXISTS (
        SELECT 1 FROM khuyen_mai
        WHERE ma_code = @ma_code
          AND trang_thai = N'HoatDong'
          AND @now BETWEEN ngay_bat_dau AND ngay_ket_thuc
          AND (so_luong_toi_da IS NULL OR so_luong_da_dung < so_luong_toi_da)
          AND @tong_tien >= don_toi_thieu
          AND (pham_vi = 'TOAN_SAN' OR (pham_vi = 'SHOP' AND ma_shop = @ma_shop))
    )
    BEGIN
        SET @hop_le = 1;
    END

    RETURN @hop_le;
END;

GO

-- Hàm: fn_TinhDiemDanhGiaTrungBinh
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Hàm 1 (Scalar): Tính điểm số sao trung bình của một cuốn sách
CREATE   FUNCTION fn_TinhDiemDanhGiaTrungBinh (@masp INT)
RETURNS DECIMAL(2,1)
AS
BEGIN
    DECLARE @diem DECIMAL(2,1);
    SELECT @diem = CAST(ROUND(AVG(CAST(so_sao AS FLOAT)), 1) AS DECIMAL(2,1))
    FROM danh_gia
    WHERE masp = @masp AND trang_thai = N'HienThi';

    RETURN ISNULL(@diem, 5.0);
END;

GO

-- Hàm: fn_LayDanhSachSachCuaShop
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Hàm 4 (Inline Table-Valued): Lấy danh sách sách của một Shop kèm danh mục
CREATE   FUNCTION fn_LayDanhSachSachCuaShop (@ma_shop INT)
RETURNS TABLE
AS
RETURN (
    SELECT 
        sp.masp,
        sp.tensp,
        sp.ten_nha_xuat_ban,
        dm.ten_danh_muc,
        sp.gia_ban,
        sp.so_luong_ton,
        sp.so_luong_da_ban,
        sp.trang_thai,
        dbo.fn_TinhDiemDanhGiaTrungBinh(sp.masp) AS diem_danh_gia
    FROM san_pham sp
    JOIN danh_muc dm ON sp.ma_danh_muc = dm.ma_danh_muc
    WHERE sp.ma_shop = @ma_shop
);

GO

-- Hàm: fn_TinhDoanhThu
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

CREATE   FUNCTION dbo.fn_TinhDoanhThu (@startDate DATETIME2, @endDate DATETIME2)
RETURNS BIGINT
AS
BEGIN
    DECLARE @tong BIGINT;
    SELECT @tong = ISNULL(SUM(CAST(tong_tien AS BIGINT)), 0)
    FROM don_hang
    WHERE ngay_dat >= @startDate AND ngay_dat <= @endDate
      AND trang_thai NOT IN (N'DaHuy', N'Huy', N'TranhChap');
    RETURN ISNULL(@tong, 0);
END;

GO

-- Hàm: fn_TinhDoanhThuKhuyenMai
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

CREATE   FUNCTION dbo.fn_TinhDoanhThuKhuyenMai (@makm INT)
RETURNS BIGINT
AS
BEGIN
    DECLARE @tong BIGINT;
    SELECT @tong = ISNULL(SUM(CAST(tong_tien AS BIGINT)), 0)
    FROM don_hang
    WHERE makm = @makm;
    RETURN ISNULL(@tong, 0);
END;

GO

-- Hàm: fn_TinhPhiSan
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

CREATE   FUNCTION dbo.fn_TinhPhiSan (@tongTien INT, @maShop INT)
RETURNS INT
AS
BEGIN
    DECLARE @pt FLOAT = 5.0;
    IF @maShop IS NOT NULL
        SELECT @pt = ISNULL(chiet_khau_phan_tram, 5.0) FROM shop WHERE ma_shop = @maShop;
    RETURN CAST(ROUND(@tongTien * (@pt / 100.0), 0) AS INT);
END;

GO

-- Hàm: fn_TinhTienGiamKhuyenMai
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

CREATE   FUNCTION dbo.fn_TinhTienGiamKhuyenMai (@makm INT)
RETURNS BIGINT
AS
BEGIN
    DECLARE @tong BIGINT;
    SELECT @tong = ISNULL(SUM(CAST(tien_giam AS BIGINT)), 0)
    FROM don_hang
    WHERE makm = @makm;
    RETURN ISNULL(@tong, 0);
END;

GO

-- Hàm: fn_TinhTienGiamVoucher
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- HÃ m 2 (Scalar): TÃ­nh sá»‘ tiá»n giáº£m giÃ¡ của một Voucher trÃªn tá»•ng tiá»n hÃ ng
CREATE   FUNCTION fn_TinhTienGiamVoucher (
    @makm INT,
    @tong_tien INT
)
RETURNS INT
AS
BEGIN
    DECLARE @loai_giam NVARCHAR(20);
    DECLARE @gia_tri INT;
    DECLARE @giam_toi_da INT;
    DECLARE @tien_giam INT = 0;

    SELECT @loai_giam = loai_giam, @gia_tri = gia_tri_giam, @giam_toi_da = giam_toi_da
    FROM khuyen_mai
    WHERE makm = @makm;

    IF @loai_giam = N'PhanTram'
    BEGIN
        SET @tien_giam = CAST(ROUND(@tong_tien * (@gia_tri / 100.0), 0) AS INT);
        IF @giam_toi_da IS NOT NULL AND @tien_giam > @giam_toi_da
            SET @tien_giam = @giam_toi_da;
    END
    ELSE IF @loai_giam = N'TienCoDinh'
    BEGIN
        SET @tien_giam = @gia_tri;
        IF @tien_giam > @tong_tien
            SET @tien_giam = @tong_tien;
    END

    RETURN @tien_giam;
END;

GO

-- Hàm: fn_TongSachDaBan
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- 1. Scalar Function: fn_TongSachDaBan
CREATE   FUNCTION dbo.fn_TongSachDaBan()
RETURNS BIGINT
AS
BEGIN
    DECLARE @total BIGINT = 0;
    SELECT @total = COALESCE(SUM(ct.so_luong), 0)
    FROM chi_tiet_don_hang ct
    JOIN don_hang dh ON ct.madh = dh.madh
    WHERE dh.trang_thai IN ('DaGiao', 'HoanTat');
    RETURN @total;
END;

GO

-- ====================================================================
-- PHẦN 3: KHUNG NHÌN (VIEWS)
-- ====================================================================

-- View: vw_BaoCaoDoanhThuTheoThang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- ====================================================================
-- PHáº¦N 3: Táº O 5 KHUNG NHÃŒN (VIEWS) BÃO CÃO Tá»”NG Há»¢P
-- ====================================================================

-- View 1: Báo cáo doanh thu toàn sàn theo tháng
CREATE   VIEW vw_BaoCaoDoanhThuTheoThang AS
SELECT 
    YEAR(ngay_dat) AS nam,
    MONTH(ngay_dat) AS thang,
    COUNT(madh) AS tong_so_don,
    SUM(CASE WHEN trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN 1 ELSE 0 END) AS so_don_thanh_cong,
    SUM(CASE WHEN trang_thai = N'DaHuy' THEN 1 ELSE 0 END) AS so_don_huy,
    SUM(CASE WHEN trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN tong_tien ELSE 0 END) AS tong_doanh_thu_hang,
    SUM(CASE WHEN trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN tien_phi_san ELSE 0 END) AS hoa_hong_san_thu,
    SUM(CASE WHEN trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN tien_thuc_nhan_shop ELSE 0 END) AS tong_tien_tra_shop
FROM don_hang
GROUP BY YEAR(ngay_dat), MONTH(ngay_dat);

GO

-- View: vw_CanhBaoTonKhoShop
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- View 4: Cảnh báo sách sắp hết hÃ ng của cÃ¡c Shop (Tá»“n <= 5)
CREATE   VIEW vw_CanhBaoTonKhoShop AS
SELECT 
    sp.masp,
    sp.tensp,
    s.ma_shop,
    s.ten_shop,
    s.so_dien_thoai AS sdt_shop,
    sp.so_luong_ton,
    sp.muc_ton_toi_thieu,
    CASE 
        WHEN sp.so_luong_ton = 0 THEN N'HẾT HÀNG'
        ELSE N'SẮP HẾT'
    END AS muc_do_canh_bao
FROM san_pham sp
JOIN shop s ON sp.ma_shop = s.ma_shop
WHERE sp.so_luong_ton <= sp.muc_ton_toi_thieu AND sp.trang_thai = N'HoatDong';

GO

-- View: vw_DoanhThuTungShop
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- View 2: Báo cáo kết quả kinh doanh của từng Shop
CREATE   VIEW vw_DoanhThuTungShop AS
SELECT 
    s.ma_shop,
    s.ten_shop,
    s.so_dien_thoai,
    s.chiet_khau_phan_tram,
    COUNT(dh.madh) AS tong_don_dat,
    SUM(CASE WHEN dh.trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN 1 ELSE 0 END) AS don_hoan_thanh,
    ISNULL(SUM(CASE WHEN dh.trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN dh.tong_tien ELSE 0 END), 0) AS tong_tien_ban,
    ISNULL(SUM(CASE WHEN dh.trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN dh.tien_phi_san ELSE 0 END), 0) AS phi_san_da_nop,
    ISNULL(SUM(CASE WHEN dh.trang_thai IN (N'GiaoThanhCong', N'HoanTat') THEN dh.tien_thuc_nhan_shop ELSE 0 END), 0) AS thuc_nhan_ve_shop
FROM shop s
LEFT JOIN don_hang dh ON s.ma_shop = dh.ma_shop
GROUP BY s.ma_shop, s.ten_shop, s.so_dien_thoai, s.chiet_khau_phan_tram;

GO

-- View: vw_TopSachBanChayToanSan
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- View 3: Top 10 cuốn sách bán cháº¡y nhất toàn sàn
CREATE   VIEW vw_TopSachBanChayToanSan AS
SELECT TOP 10
    sp.masp,
    sp.tensp,
    s.ten_shop,
    dm.ten_danh_muc,
    sp.gia_ban,
    sp.so_luong_ton,
    sp.so_luong_da_ban,
    (sp.gia_ban * sp.so_luong_da_ban) AS tong_doanh_thu_sach
FROM san_pham sp
JOIN shop s ON sp.ma_shop = s.ma_shop
JOIN danh_muc dm ON sp.ma_danh_muc = dm.ma_danh_muc
ORDER BY sp.so_luong_da_ban DESC;

GO

-- View: vw_ThongKeDanhGiaSanPham
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- View 5: Thống kê đánh giá sách
CREATE   VIEW vw_ThongKeDanhGiaSanPham AS
SELECT 
    sp.masp,
    sp.tensp,
    s.ten_shop,
    COUNT(dg.ma_danh_gia) AS tong_luot_danh_gia,
    ISNULL(CAST(ROUND(AVG(CAST(dg.so_sao AS FLOAT)), 1) AS DECIMAL(2,1)), 5.0) AS diem_sao_trung_binh
FROM san_pham sp
JOIN shop s ON sp.ma_shop = s.ma_shop
LEFT JOIN danh_gia dg ON sp.masp = dg.masp AND dg.trang_thai = N'HienThi'
GROUP BY sp.masp, sp.tensp, s.ten_shop;

GO

-- ====================================================================
-- PHẦN 4: THỦ TỤC LƯU TRỮ (STORED PROCEDURES)
-- ====================================================================

-- Thủ tục: sp_DatHangVaTruTonKho
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- ====================================================================
-- PHẦN 6: BẢO MẬT & PHÂN QUYỀN (ROLES & USERS)
-- ====================================================================

-- Thá»§ tá»¥c 1: Äáº·t hÃ ng hoàn tất (Tạo đơn + Trá»« tồn kho + Ghi nháº­n voucher)
CREATE   PROCEDURE sp_DatHangVaTruTonKho
    @makh INT,
    @ma_shop INT,
    @ma_nvc INT,
    @makm INT = NULL,
    @dia_chi_giao NVARCHAR(255),
    @so_dien_thoai_giao VARCHAR(20),
    @tong_tien INT,
    @phi_van_chuyen INT = 0,
    @tien_giam INT = 0,
    @phuong_thuc NVARCHAR(30) = N'COD',
    @new_madh INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Lấy tỷ lệ chiết khấu của Shop
        DECLARE @chiet_khau DECIMAL(5,2) = 5.00;
        SELECT @chiet_khau = chiet_khau_phan_tram FROM shop WHERE ma_shop = @ma_shop;

        -- 2. Thêm đơn hàng
        INSERT INTO don_hang (
            makh, ma_shop, ma_nvc, makm, dia_chi_giao, so_dien_thoai_giao,
            tong_tien, phi_van_chuyen, tien_giam, chiet_khau_app_phan_tram,
            phuong_thuc_thanh_toan, trang_thai
        )
        VALUES (
            @makh, @ma_shop, @ma_nvc, @makm, @dia_chi_giao, @so_dien_thoai_giao,
            @tong_tien, @phi_van_chuyen, @tien_giam, @chiet_khau,
            @phuong_thuc, N'ChoXacNhan'
        );

        SET @new_madh = SCOPE_IDENTITY();

        -- 3. Cáº­p nháº­t lượt sá»­ dá»¥ng của voucher (nếu có)
        IF @makm IS NOT NULL
        BEGIN
            UPDATE khuyen_mai
            SET so_luong_da_dung = so_luong_da_dung + 1
            WHERE makm = @makm;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR (@ErrMsg, 16, 1);
    END CATCH
END;

GO

-- Thủ tục: sp_DoanhThuShopTheoThang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- 3. Stored Procedure: sp_DoanhThuShopTheoThang
CREATE   PROCEDURE dbo.sp_DoanhThuShopTheoThang
    @p_ma_shop INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        YEAR(dh.ngay_dat) AS nam,
        MONTH(dh.ngay_dat) AS thang,
        COUNT(dh.madh) AS tong_don_hang,
        COALESCE(SUM(dh.tong_tien), 0) AS tong_doanh_thu,
        COALESCE(SUM(dh.tien_phi_san), 0) AS tong_phi_san,
        COALESCE(SUM(dh.tien_thuc_nhan_shop), 0) AS thuc_nhan_shop
    FROM don_hang dh
    WHERE dh.ma_shop = @p_ma_shop
      AND dh.trang_thai IN ('DaGiao', 'DA_GIAO', 'HoanTat')
    GROUP BY YEAR(dh.ngay_dat), MONTH(dh.ngay_dat)
    ORDER BY nam DESC, thang DESC;
END;

GO

-- Thủ tục: sp_DoanhThuTheoThang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- 4. Stored Procedure: sp_DoanhThuTheoThang
CREATE   PROCEDURE dbo.sp_DoanhThuTheoThang
AS
BEGIN
    SET NOCOUNT ON;
    SELECT YEAR(dh.ngay_hoan_thanh) as nam, 
           MONTH(dh.ngay_hoan_thanh) as thang, 
           SUM(dh.tong_tien) as doanh_thu 
    FROM don_hang dh 
    WHERE dh.trang_thai IN ('DaGiao', 'HoanTat') 
      AND dh.ngay_hoan_thanh IS NOT NULL 
      AND YEAR(dh.ngay_hoan_thanh) = YEAR(GETDATE())
    GROUP BY YEAR(dh.ngay_hoan_thanh), MONTH(dh.ngay_hoan_thanh) 
    ORDER BY nam DESC, thang DESC;
END;

GO

-- Thủ tục: sp_DonDepSanPhamDaXem
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
CREATE   PROCEDURE sp_DonDepSanPhamDaXem @SoGioToiDa INT = 1 AS BEGIN SET NOCOUNT ON; DECLARE @ThoiDiemHetHan DATETIME2 = DATEADD(HOUR, -@SoGioToiDa, GETDATE()); DELETE FROM san_pham_da_xem WHERE thoi_gian_xem < @ThoiDiemHetHan; SELECT @@ROWCOUNT AS SoBanGhiDaDonDep; END;
GO

-- Thủ tục: sp_HuyDonHang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Thá»§ tá»¥c 3: Hủy đơn hàng vÃ  hoÃ n tiá»n/voucher
CREATE   PROCEDURE sp_HuyDonHang
    @madh INT,
    @ly_do NVARCHAR(500)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @trang_thai_hien_tai NVARCHAR(30);
        DECLARE @makm INT;

        SELECT @trang_thai_hien_tai = trang_thai, @makm = makm
        FROM don_hang
        WHERE madh = @madh;

        IF @trang_thai_hien_tai IN (N'DangGiao', N'GiaoThanhCong', N'HoanTat')
        BEGIN
            RAISERROR (N'ÄÆ¡n hÃ ng Ä‘ang giao hoặc đã hoàn tất, không thá»ƒ há»§y!', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Cáº­p nháº­t trạng thái đơn sang 'DaHuy' (Trigger trg_DonHang_HoanTonKhoKhiHuy sáº½ tá»± hoÃ n tồn kho)
        UPDATE don_hang
        SET trang_thai = N'DaHuy',
            ly_do_huy = @ly_do
        WHERE madh = @madh;

        -- HoÃ n láº¡i lượt dÃ¹ng voucher (nếu có)
        IF @makm IS NOT NULL
        BEGIN
            UPDATE khuyen_mai
            SET so_luong_da_dung = CASE WHEN so_luong_da_dung > 0 THEN so_luong_da_dung - 1 ELSE 0 END
            WHERE makm = @makm;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR (@ErrMsg, 16, 1);
    END CATCH
END;

GO

-- Thủ tục: sp_ShopXacNhanDonHang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
CREATE   PROCEDURE sp_ShopXacNhanDonHang @madh INT, @ma_shop INT, @ma_van_don VARCHAR(50) AS BEGIN SET NOCOUNT ON; BEGIN TRY BEGIN TRANSACTION; IF NOT EXISTS (SELECT 1 FROM don_hang WHERE madh = @madh AND ma_shop = @ma_shop AND trang_thai IN (N'ChoXacNhan', N'ChoXuLy')) BEGIN RAISERROR (N'Đơn hàng không hợp lệ hoặc không ở trạng thái Chờ xác nhận!', 16, 1); ROLLBACK TRANSACTION; RETURN; END UPDATE don_hang SET trang_thai = N'DangGiao', ngay_xac_nhan = SYSUTCDATETIME(), ma_van_don = @ma_van_don WHERE madh = @madh; COMMIT TRANSACTION; END TRY BEGIN CATCH IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION; DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE(); RAISERROR (@ErrMsg, 16, 1); END CATCH END;
GO

-- Thủ tục: sp_ShopXacNhanKiemKe
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Thá»§ tá»¥c 2: Shop xÃ¡c nháº­n vÃ  phê duyệt phiếu kiểm kê sản phẩm
CREATE   PROCEDURE sp_ShopXacNhanKiemKe
    @ma_phieu_kiem_ke INT,
    @ma_shop INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Kiểm tra quyá»n sở hữu phiếu kiểm kê
        IF NOT EXISTS (SELECT 1 FROM phieu_kiem_ke WHERE ma_phieu_kiem_ke = @ma_phieu_kiem_ke AND ma_shop = @ma_shop)
        BEGIN
            RAISERROR (N'Phiáº¿u kiểm kê không tá»“n táº¡i hoặc không thuộc quyá»n quản lý của Shop!', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Chuyá»ƒn trạng thái sang 'DaCanBang' (Trigger trg_PhieuKiemKe_CanBangTonKho sáº½ tự động cáº­p nháº­t số lượng tá»“n vào san_pham)
        UPDATE phieu_kiem_ke
        SET trang_thai = N'DaCanBang',
            ngay_phe_duyet = SYSUTCDATETIME()
        WHERE ma_phieu_kiem_ke = @ma_phieu_kiem_ke;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR (@ErrMsg, 16, 1);
    END CATCH
END;

GO

-- Thủ tục: sp_TimKiemSachPhanTrang
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- Thá»§ tá»¥c 5: Tìm kiếm sách nâng cao có phí¢n trang
CREATE   PROCEDURE sp_TimKiemSachPhanTrang
    @tu_khoa NVARCHAR(100) = NULL,
    @ma_danh_muc INT = NULL,
    @gia_tu INT = NULL,
    @gia_den INT = NULL,
    @page_index INT = 1,
    @page_size INT = 12
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        DECLARE @offset INT = (@page_index - 1) * @page_size;

        SELECT 
            sp.masp,
            sp.tensp,
            sp.ten_nha_xuat_ban,
            sp.gia_ban,
            sp.hinh_anh,
            sp.so_luong_ton,
            sp.so_luong_da_ban,
            s.ma_shop,
            s.ten_shop,
            dm.ten_danh_muc,
            dbo.fn_TinhDiemDanhGiaTrungBinh(sp.masp) AS diem_sao
        FROM san_pham sp
        JOIN shop s ON sp.ma_shop = s.ma_shop
        JOIN danh_muc dm ON sp.ma_danh_muc = dm.ma_danh_muc
        WHERE sp.trang_thai = N'HoatDong'
          AND s.trang_thai = N'HoatDong'
          AND (@tu_khoa IS NULL OR sp.tensp LIKE N'%' + @tu_khoa + N'%' OR sp.ten_nha_xuat_ban LIKE N'%' + @tu_khoa + N'%')
          AND (@ma_danh_muc IS NULL OR sp.ma_danh_muc = @ma_danh_muc)
          AND (@gia_tu IS NULL OR sp.gia_ban >= @gia_tu)
          AND (@gia_den IS NULL OR sp.gia_ban <= @gia_den)
        ORDER BY sp.so_luong_da_ban DESC, sp.masp DESC
        OFFSET @offset ROWS
        FETCH NEXT @page_size ROWS ONLY;
    END TRY
    BEGIN CATCH
        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR (@ErrMsg, 16, 1);
    END CATCH
END;

GO

-- Thủ tục: sp_TopSachBanChay
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO

-- 5. Stored Procedure: sp_TopSachBanChay
CREATE   PROCEDURE dbo.sp_TopSachBanChay
    @p_limit INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    SELECT TOP (@p_limit)
        s.masp,
        s.tensp,
        SUM(ct.so_luong) as tong_da_ban,
        s.gia_ban,
        SUM(ct.so_luong * ct.don_gia) as doanh_thu_dong_gop
    FROM san_pham s
    JOIN chi_tiet_don_hang ct ON s.masp = ct.masp
    JOIN don_hang dh ON ct.madh = dh.madh
    WHERE dh.trang_thai IN ('DaGiao', 'HoanTat')
    GROUP BY s.masp, s.tensp, s.gia_ban
    ORDER BY tong_da_ban DESC;
END;

GO

-- ====================================================================
-- PHáº¦N 5: Dá»® LIá»†U MáºªU Äáº¦Y Äá»¦ (SEED DATA)
-- ====================================================================

-- Dữ liệu bảng: tai_khoan (7 dòng)
SET IDENTITY_INSERT [dbo].[tai_khoan] ON 

INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (1, N'admin', N'admin@the4bookstore.vn', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'ADMIN', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-16T15:04:53.1338583' AS DateTime2))
INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (3, N'vendor_kimdong', N'kimdong@gmail.com', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'VENDOR', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-16T15:04:53.1338583' AS DateTime2))
INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (4, N'vendor_nhanam', N'nhanam@gmail.com', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'VENDOR', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-16T15:04:53.1338583' AS DateTime2))
INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (5, N'khachhang_an', N'nguyenvanan@gmail.com', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'KHACHHANG', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-16T15:04:53.1338583' AS DateTime2))
INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (6, N'khachhang_binh', N'lethibinh@gmail.com', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'KHACHHANG', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-16T15:04:53.1338583' AS DateTime2))
INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (7, N'phat', N'thangphatnekea21333@gmail.com', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'KHACHHANG', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-17T10:05:28.7073572' AS DateTime2))
INSERT [dbo].[tai_khoan] ([ma_tai_khoan], [ten_dang_nhap], [email], [mat_khau_hash], [vai_tro], [trang_thai], [auth_provider], [provider_id], [token_version], [otp_code], [otp_expiry], [ngay_tao]) VALUES (8, N'manager', N'manager@the4bookstore.vn', N'$2a$10$3QaVEi2S62h8gVvIg3JHfuTDnLvXvD4RV7kU6bwvOg9lsD.3CeTEG', N'MANAGER', N'HoatDong', N'LOCAL', NULL, 1, NULL, NULL, CAST(N'2026-10-17T03:37:45.5024198' AS DateTime2))
SET IDENTITY_INSERT [dbo].[tai_khoan] OFF
GO

-- Dữ liệu bảng: khach_hang (7 dòng)
SET IDENTITY_INSERT [dbo].[khach_hang] ON 

INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (1, 5, N'Nguyễn Văn An', N'nguyenvanan@gmail.com', N'0988111222', N'123 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', NULL, CAST(N'2026-10-16T15:04:53.1388745' AS DateTime2))
INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (2, 6, N'Lê Thị Bình', N'lethibinh@gmail.com', N'0977333444', N'456 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh', NULL, CAST(N'2026-10-16T15:04:53.1388745' AS DateTime2))
INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (3, 7, N'Vo Thanh Phat', N'thangphatnekea21333@gmail.com', N'0967238940', N'Dĩ An', NULL, CAST(N'2026-10-17T10:05:28.8814825' AS DateTime2))
INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (4, 3, N'vendor_kimdong', N'kimdong@gmail.com', NULL, NULL, NULL, CAST(N'2026-10-17T10:09:24.4003166' AS DateTime2))
INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (5, 1, N'admin', N'admin@the4bookstore.vn', NULL, NULL, NULL, CAST(N'2026-10-17T10:22:00.7897696' AS DateTime2))
INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (7, 8, N'manager', N'manager@the4bookstore.vn', NULL, NULL, NULL, CAST(N'2026-10-17T11:01:50.1268754' AS DateTime2))
INSERT [dbo].[khach_hang] ([makh], [ma_tai_khoan], [ho_ten], [email], [so_dien_thoai], [dia_chi], [anh_dai_dien], [ngay_dang_ky]) VALUES (8, 4, N'vendor_nhanam', N'nhanam@gmail.com', NULL, NULL, NULL, CAST(N'2026-10-17T11:56:01.8117393' AS DateTime2))
SET IDENTITY_INSERT [dbo].[khach_hang] OFF
GO

-- Dữ liệu bảng: dia_chi_giao_hang (3 dòng)
SET IDENTITY_INSERT [dbo].[dia_chi_giao_hang] ON 

INSERT [dbo].[dia_chi_giao_hang] ([ma_dia_chi], [makh], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_chi_tiet], [phuong_xa], [quan_huyen], [tinh_thanh], [la_mac_dinh], [ngay_tao]) VALUES (1, 1, N'Nguyễn Văn An', N'0988111222', N'123 Lê Lợi, P. Bến Nghé, Quận 1', NULL, NULL, N'TP. Hồ Chí Minh', 1, CAST(N'2026-10-16T15:04:53.1403843' AS DateTime2))
INSERT [dbo].[dia_chi_giao_hang] ([ma_dia_chi], [makh], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_chi_tiet], [phuong_xa], [quan_huyen], [tinh_thanh], [la_mac_dinh], [ngay_tao]) VALUES (2, 2, N'Lê Thị Bình', N'0977333444', N'456 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức', NULL, NULL, N'TP. Hồ Chí Minh', 1, CAST(N'2026-10-16T15:04:53.1403843' AS DateTime2))
INSERT [dbo].[dia_chi_giao_hang] ([ma_dia_chi], [makh], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_chi_tiet], [phuong_xa], [quan_huyen], [tinh_thanh], [la_mac_dinh], [ngay_tao]) VALUES (3, 3, N'Nguyễn Ngọc Thiện', N')934567822', N'462', N'Linh trung', N'TP.thủ đức', N'Hồ Chí minh', 1, CAST(N'2026-10-17T11:08:22.0698431' AS DateTime2))
SET IDENTITY_INSERT [dbo].[dia_chi_giao_hang] OFF
GO

-- Dữ liệu bảng: shop (2 dòng)
SET IDENTITY_INSERT [dbo].[shop] ON 

INSERT [dbo].[shop] ([ma_shop], [ma_tai_khoan], [ten_shop], [slug], [logo], [banner], [mo_ta], [dia_chi_shop], [so_dien_thoai], [email_shop], [chiet_khau_phan_tram], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (1, 3, N'Hiệu Sách Kim Đồng Official', N'hieu-sach-kim-dong', N'https://res.cloudinary.com/vkqtqiha/image/upload/v1792206732/the4bookstore/shops/ypnxy2kltgf1o9bo6pvr.png', N'https://res.cloudinary.com/vkqtqiha/image/upload/v1792206734/the4bookstore/shops/zwhzbwbgrnmcgot7koac.jpg', N'Gian hàng chính hãng truyện tranh, văn học thiếu nhi của NXB Kim Đồng', N'55 Quang Trung, Hai Bà Trưng, Hà Nội', N'02439434730', N'kimdong@gmail.com', CAST(5.00 AS Decimal(5, 2)), N'HoatDong', CAST(N'2026-10-16T15:04:53.1424040' AS DateTime2), CAST(N'2026-10-17T10:12:18.7290932' AS DateTime2))
INSERT [dbo].[shop] ([ma_shop], [ma_tai_khoan], [ten_shop], [slug], [logo], [banner], [mo_ta], [dia_chi_shop], [so_dien_thoai], [email_shop], [chiet_khau_phan_tram], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (2, 4, N'Nhã Nam Book Store', N'nha-nam-book-store', NULL, NULL, N'Nhã Nam - Nhận diện tác phẩm kinh điển và văn học tinh hoa thế giới', N'59 Đỗ Quang, Cầu Giấy, Hà Nội', N'02435146875', N'nhanam@gmail.com', CAST(5.00 AS Decimal(5, 2)), N'HoatDong', CAST(N'2026-10-16T15:04:53.1424040' AS DateTime2), NULL)
SET IDENTITY_INSERT [dbo].[shop] OFF
GO

-- Dữ liệu bảng: danh_muc (6 dòng)
SET IDENTITY_INSERT [dbo].[danh_muc] ON 

INSERT [dbo].[danh_muc] ([ma_danh_muc], [ten_danh_muc], [mo_ta], [trang_thai]) VALUES (1, N'Văn Học Trong Nước', N'Tiểu thuyết, truyện ngắn, phóng sự và tác phẩm văn học Việt Nam', 1)
INSERT [dbo].[danh_muc] ([ma_danh_muc], [ten_danh_muc], [mo_ta], [trang_thai]) VALUES (2, N'Văn Học Nước Ngoài', N'Tác phẩm dịch kinh điển, văn học thế giới đạt giải Nobel, Booker', 1)
INSERT [dbo].[danh_muc] ([ma_danh_muc], [ten_danh_muc], [mo_ta], [trang_thai]) VALUES (3, N'Kinh Tế - Khởi Nghiệp', N'Quản trị kinh doanh, đầu tư, tài chính và khởi nghiệp đổi mới', 1)
INSERT [dbo].[danh_muc] ([ma_danh_muc], [ten_danh_muc], [mo_ta], [trang_thai]) VALUES (4, N'Công Nghệ Thông Tin', N'Lập trình, kiến trúc phần mềm, khoa học dữ liệu và an toàn thông tin', 1)
INSERT [dbo].[danh_muc] ([ma_danh_muc], [ten_danh_muc], [mo_ta], [trang_thai]) VALUES (5, N'Thiếu Nhi & Truyện Tranh', N'Manga, comic, truyện thiếu nhi và văn học giáo dục sớm', 1)
INSERT [dbo].[danh_muc] ([ma_danh_muc], [ten_danh_muc], [mo_ta], [trang_thai]) VALUES (6, N'Tâm Lý & Kỹ Năng Sống', N'Sách phát triển bản thân, tư duy tích cực và thấu hiểu nội tâm', 1)
SET IDENTITY_INSERT [dbo].[danh_muc] OFF
GO

-- Dữ liệu bảng: tac_gia (8 dòng)
SET IDENTITY_INSERT [dbo].[tac_gia] ON 

INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (1, N'Nguyễn Nhật Ánh', N'Nhà văn chuyên viết cho thanh thiếu niên được mến mộ bậc nhất Việt Nam')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (2, N'Tô Hoài', N'Nhà văn đại thụ của nền văn học thiếu nhi Việt Nam')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (3, N'Dale Carnegie', N'Tác giả bậc thầy người Mỹ về nghệ thuật giao tiếp và đối nhân xử thế')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (4, N'Haruki Murakami', N'Nhà văn đương đại lừng danh người Nhật Bản')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (5, N'Robert C. Martin', N'Huyền thoại Uncle Bob - tác giả của Clean Code và kiến trúc phần mềm')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (6, N'Antoine de Saint-Exupéry', N'Nhà văn và phi công quả cảm người Pháp, tác giả Hoàng Tử Bé')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (7, N'Paulo Coelho', N'Nhà văn nổi tiếng thế giới người Brazil, tác giả Nhà Giả Kim')
INSERT [dbo].[tac_gia] ([ma_tac_gia], [ten_tac_gia], [mo_ta]) VALUES (8, N'Morgan Housel', N'Chuyên gia tài chính và tác giả bán chạy toàn cầu về tâm lý học tiền bạc')
SET IDENTITY_INSERT [dbo].[tac_gia] OFF
GO

-- Dữ liệu bảng: nha_xuat_ban (5 dòng)
SET IDENTITY_INSERT [dbo].[nha_xuat_ban] ON 

INSERT [dbo].[nha_xuat_ban] ([manxb], [tennxb], [dia_chi], [so_dien_thoai], [email]) VALUES (1, N'NXB Kim Äá»“ng', N'55 Quang Trung, Hà Nội', N'02439434730', N'cskh@nxbkimdong.com.vn')
INSERT [dbo].[nha_xuat_ban] ([manxb], [tennxb], [dia_chi], [so_dien_thoai], [email]) VALUES (2, N'NXB Trẻ', N'161B LÃ½ ChÃ­nh Tháº¯ng, Q3, TP.HCM', N'02839316289', N'hopthu@nxbtre.com.vn')
INSERT [dbo].[nha_xuat_ban] ([manxb], [tennxb], [dia_chi], [so_dien_thoai], [email]) VALUES (3, N'NXB Phụ Nữ', N'39 Hàng Chuối, Hà Nội', N'02439710723', N'truyenthong@nxbphunu.com.vn')
INSERT [dbo].[nha_xuat_ban] ([manxb], [tennxb], [dia_chi], [so_dien_thoai], [email]) VALUES (4, N'NXB Tổng Hợp TP.HCM', N'62 Nguyễn Thị Minh Khai, Q1, TP.HCM', N'02838225340', N'tonghop@nxbhcm.com.vn')
INSERT [dbo].[nha_xuat_ban] ([manxb], [tennxb], [dia_chi], [so_dien_thoai], [email]) VALUES (5, N'NXB Giáo Dục Việt Nam', N'81 Tráº§n HÆ°ng Äáº¡o, HÃ  Ná»™i', N'02438220801', N'contact@nxbgd.vn')
SET IDENTITY_INSERT [dbo].[nha_xuat_ban] OFF
GO

-- Dữ liệu bảng: san_pham (14 dòng)
SET IDENTITY_INSERT [dbo].[san_pham] ON 

INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (1, 1, 1, N'Mắt Biếc', N'NXB Trẻ', N'9786041071234', 110000, 47, 18, 5, N'Sach', N'Câu chuyện tình yêu tuổi học trò sâu lắng và day dứt của Ngạn và Hà Lan nơi làng Đo Đo mộc mạc.', N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T15:04:53.1483971' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (2, 1, 1, N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', N'NXB Trẻ', N'9786041054321', 125000, 33, 22, 5, N'Sach', N'Tấm vé trở về tuổi thơ nơi miền quê nghèo êm đềm với những ký ức ngọt ngào và tình anh em ruột thịt.', N'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T15:04:53.1483971' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (3, 1, 5, N'Dế Mèn Phiêu Lưu Ký', N'NXB Kim Đồng', N'9786042087654', 65000, 79, 43, 5, N'Sach', N'Tác phẩm thiếu nhi kinh điển của nhà văn Tô Hoài về bài học đường đời đầu tiên và tình bạn cao đẹp.', N'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T15:04:53.1483971' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (4, 2, 2, N'Rừng Na Uy', N'NXB Hội Nhà Văn', N'9786045612345', 135000, 38, 30, 5, N'Sach', N'Kiệt tác nổi tiếng nhất của Haruki Murakami về nỗi cô đơn, tình yêu và tuổi trẻ đầy trăn trở tại Tokyo.', N'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T15:04:53.1483971' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (5, 2, 3, N'Đắc Nhân Tâm', N'NXB Tổng Hợp TP.HCM', N'9786045876543', 95000, 100, 65, 5, N'Sach', N'Tác phẩm kinh điển vượt thời gian về nghệ thuật thu phục lòng người và kỹ năng giao tiếp chân thành.', N'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T15:04:53.1483971' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (6, 2, 4, N'Clean Code - Mã Sạch', N'NXB Thông Tin & Truyền Thông', N'9780132350884', 250000, 19, 13, 5, N'Sach', N'Cẩm nang nâng cao chất lượng mã nguồn dành cho mọi kỹ sư phần mềm chuyên nghiệp hướng đến sự tinh gọn.', N'https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T15:04:53.1483971' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (7, 2, 2, N'Hoàng Tử Bé', N'NXB Hội Nhà Văn', N'9786045634567', 75000, 60, 38, 5, N'Sach', N'Một câu chuyện ngụ ngôn tuyệt mỹ dành cho cả trẻ em lẫn người lớn về tình yêu thương và sự gắn kết.', N'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1719499' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (8, 2, 2, N'Nhà Giả Kim', N'NXB Văn Học', N'9786046987654', 85000, 90, 85, 5, N'Sach', N'Hành trình theo đuổi vận mệnh và giấc mơ cá nhân của chàng trai chăn cừu Santiago qua sa mạc mênh mông.', N'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1739494' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (9, 2, 3, N'Tâm Lý Học Về Tiền', N'NXB Lao Động', N'9786045987123', 145000, 45, 52, 5, N'Sach', N'Những bí mật về cách con người suy nghĩ, cảm nhận và đưa ra quyết định với tiền bạc trong đời sống thực tế.', N'https://images.unsplash.com/photo-1553729459-efe14ef6055d?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1739494' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (10, 1, 1, N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', N'NXB Trẻ', N'9786041088899', 95000, 68, 36, 5, N'Sach', N'Tác phẩm đưa người đọc quay trở lại thế giới ngây ngô, tươi vui của tuổi ấu thơ với đầy ắp tiếng cười.', N'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1749512' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (11, 1, 5, N'Doraemon Tuyển Tập Tranh Truyện Màu', N'NXB Kim Đồng', N'9786042123456', 55000, 120, 95, 5, N'Sach', N'Tập truyện tranh màu đặc sắc kể về chú mèo máy thông minh đến từ thế kỷ 22 và nhóm bạn Nobita.', N'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1749512' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (12, 2, 4, N'Kiến Trúc Phần Mềm Thực Chiến', N'NXB Thông Tin & Truyền Thông', N'9786048033221', 280000, 30, 19, 5, N'Sach', N'Hướng dẫn chi tiết xây dựng hệ thống phân tán, Microservices và thiết kế CSDL chịu tải cao.', N'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1759510' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (13, 2, 4, N'Sổ Tay Da Cao Cấp Nhã Nam Edition', N'Nhã Nam Studio', N'VPP-9001', 89000, 49, 23, 5, N'VanPhongPham', N'Sổ tay bìa da PU cao cấp, giấy chống lóa 100gsm chuyên dụng ghi chép sáng tạo.', N'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1769497' AS DateTime2))
INSERT [dbo].[san_pham] ([masp], [ma_shop], [ma_danh_muc], [tensp], [ten_nha_xuat_ban], [isbn], [gia_ban], [so_luong_ton], [so_luong_da_ban], [muc_ton_toi_thieu], [loaisp], [mo_ta], [hinh_anh], [trang_thai], [trang_thai_khoa], [ngay_tao]) VALUES (14, 1, 5, N'Đèn Đọc Sách Mini Kẹp Bìa Sách LED', N'Kim Đồng Goods', N'QT-8001', 65000, 37, 18, 5, N'QuaTang', N'Đèn LED kẹp sách 3 mức sáng bảo vệ mắt ban đêm, pin sạc USB tiện dụng.', N'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=600', N'DangBan', N'BinhThuong', CAST(N'2026-10-16T16:31:25.1769497' AS DateTime2))
SET IDENTITY_INSERT [dbo].[san_pham] OFF
GO

-- Dữ liệu bảng: san_pham_tac_gia (12 dòng)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (1, 1, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (2, 1, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (3, 2, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (4, 4, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (5, 3, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (6, 5, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (7, 6, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (8, 7, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (9, 8, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (10, 1, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (11, 2, 1)
INSERT [dbo].[san_pham_tac_gia] ([masp], [ma_tac_gia], [thu_tu_tac_gia]) VALUES (12, 5, 1)
GO

-- Dữ liệu bảng: phieu_kiem_ke (1 dòng)
SET IDENTITY_INSERT [dbo].[phieu_kiem_ke] ON 

INSERT [dbo].[phieu_kiem_ke] ([ma_phieu_kiem_ke], [ma_shop], [ngay_kiem_ke], [ngay_phe_duyet], [trang_thai], [ghi_chu]) VALUES (1, 1, CAST(N'2026-10-01T00:00:00.0000000' AS DateTime2), NULL, N'DangKiemKe', N'Kiá»ƒm kÃª Ä‘á»‹nh ká»³ Ä‘áº§u tháng 10 của Shop Kim Äá»“ng')
SET IDENTITY_INSERT [dbo].[phieu_kiem_ke] OFF
GO

-- Dữ liệu bảng: chi_tiet_kiem_ke (2 dòng)
INSERT [dbo].[chi_tiet_kiem_ke] ([ma_phieu_kiem_ke], [masp], [so_luong_he_thong], [so_luong_thuc_te], [chenh_lech], [ly_do]) VALUES (1, 1, 50, 48, -2, N'Rách bìa 2 cuốn phải thanh lý')
INSERT [dbo].[chi_tiet_kiem_ke] ([ma_phieu_kiem_ke], [masp], [so_luong_he_thong], [so_luong_thuc_te], [chenh_lech], [ly_do]) VALUES (1, 2, 35, 35, 0, N'Khớp số lượng')
GO

-- Dữ liệu bảng: nha_van_chuyen (3 dòng)
SET IDENTITY_INSERT [dbo].[nha_van_chuyen] ON 

INSERT [dbo].[nha_van_chuyen] ([ma_nvc], [ten_nvc], [phi_co_ban], [thoi_gian_du_kien], [trang_thai]) VALUES (1, N'Giao Hàng Tiết Kiệm (GHTK)', 22000, N'1 - 2 ngày làm việc', N'HoatDong')
INSERT [dbo].[nha_van_chuyen] ([ma_nvc], [ten_nvc], [phi_co_ban], [thoi_gian_du_kien], [trang_thai]) VALUES (2, N'Giao Hàng Nhanh (GHN)', 25000, N'2 - 3 ngày làm việc', N'HoatDong')
INSERT [dbo].[nha_van_chuyen] ([ma_nvc], [ten_nvc], [phi_co_ban], [thoi_gian_du_kien], [trang_thai]) VALUES (3, N'Viettel Post', 28000, N'2 - 4 ngày làm việc', N'HoatDong')
SET IDENTITY_INSERT [dbo].[nha_van_chuyen] OFF
GO

-- Dữ liệu bảng: khuyen_mai (4 dòng)
SET IDENTITY_INSERT [dbo].[khuyen_mai] ON 

INSERT [dbo].[khuyen_mai] ([makm], [ma_code], [tenkm], [mo_ta], [loai_giam], [gia_tri_giam], [giam_toi_da], [don_toi_thieu], [so_luong_toi_da], [so_luong_da_dung], [pham_vi], [ma_shop], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [hien_thi_cong_khai], [gioi_han_moi_khach_hang], [doi_tuong_khach_hang], [loai_khuyen_mai]) VALUES (1, N'TOANSAN10', N'Giảm 10% Toàn Sàn', N'Áp dụng cho mọi đơn hàng từ 200.000đ trở lên', N'PhanTram', 10, 50000, 200000, NULL, 0, N'TOAN_SAN', NULL, CAST(N'2025-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2027-12-31T00:00:00.0000000' AS DateTime2), N'HoatDong', 1, 1, N'ALL', N'GIAM_GIA_SAN_PHAM')
INSERT [dbo].[khuyen_mai] ([makm], [ma_code], [tenkm], [mo_ta], [loai_giam], [gia_tri_giam], [giam_toi_da], [don_toi_thieu], [so_luong_toi_da], [so_luong_da_dung], [pham_vi], [ma_shop], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [hien_thi_cong_khai], [gioi_han_moi_khach_hang], [doi_tuong_khach_hang], [loai_khuyen_mai]) VALUES (2, N'FREESHIP', N'Miễn Phí Vận Chuyển', N'Giảm 25.000đ phí giao hàng cho đơn từ 150.000đ', N'TienCoDinh', 25000, 25000, 150000, NULL, 0, N'TOAN_SAN', NULL, CAST(N'2025-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2027-12-31T00:00:00.0000000' AS DateTime2), N'HoatDong', 1, 1, N'ALL', N'GIAM_GIA_SAN_PHAM')
INSERT [dbo].[khuyen_mai] ([makm], [ma_code], [tenkm], [mo_ta], [loai_giam], [gia_tri_giam], [giam_toi_da], [don_toi_thieu], [so_luong_toi_da], [so_luong_da_dung], [pham_vi], [ma_shop], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [hien_thi_cong_khai], [gioi_han_moi_khach_hang], [doi_tuong_khach_hang], [loai_khuyen_mai]) VALUES (3, N'KIMDONG20K', N'Ưu Đãi Tri Ân Kim Đồng', N'Giảm ngay 20.000đ cho đơn hàng truyện tranh Kim Đồng từ 100.000đ', N'TienCoDinh', 20000, 20000, 100000, NULL, 0, N'SHOP', 1, CAST(N'2025-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2027-12-31T00:00:00.0000000' AS DateTime2), N'HoatDong', 1, 1, N'ALL', N'GIAM_GIA_SAN_PHAM')
INSERT [dbo].[khuyen_mai] ([makm], [ma_code], [tenkm], [mo_ta], [loai_giam], [gia_tri_giam], [giam_toi_da], [don_toi_thieu], [so_luong_toi_da], [so_luong_da_dung], [pham_vi], [ma_shop], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [hien_thi_cong_khai], [gioi_han_moi_khach_hang], [doi_tuong_khach_hang], [loai_khuyen_mai]) VALUES (4, N'NHANAM15', N'Đặc Quyền Hội Sách Nhã Nam', N'Giảm 15% tối đa 40.000đ cho các đầu sách văn học Nhã Nam', N'PhanTram', 15, 60000, 250000, NULL, 0, N'SHOP', 2, CAST(N'2025-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2027-12-31T00:00:00.0000000' AS DateTime2), N'HoatDong', 1, 1, N'ALL', N'GIAM_GIA_SAN_PHAM')
SET IDENTITY_INSERT [dbo].[khuyen_mai] OFF
GO

-- Dữ liệu bảng: voucher_da_luu (2 dòng)
SET IDENTITY_INSERT [dbo].[voucher_da_luu] ON 

INSERT [dbo].[voucher_da_luu] ([id], [makh], [makm], [trang_thai], [ngay_luu], [ngay_su_dung]) VALUES (1, 3, 1, N'ChuaDung', CAST(N'2026-10-17T11:07:01.8027727' AS DateTime2), NULL)
INSERT [dbo].[voucher_da_luu] ([id], [makh], [makm], [trang_thai], [ngay_luu], [ngay_su_dung]) VALUES (2, 3, 2, N'ChuaDung', CAST(N'2026-10-17T11:07:03.5162588' AS DateTime2), NULL)
SET IDENTITY_INSERT [dbo].[voucher_da_luu] OFF
GO

-- Dữ liệu bảng: gio_hang (2 dòng)
SET IDENTITY_INSERT [dbo].[gio_hang] ON 

INSERT [dbo].[gio_hang] ([ma_gio_hang], [makh], [ngay_tao], [ngay_cap_nhat]) VALUES (1, 1, CAST(N'2026-10-16T22:27:33.1731952' AS DateTime2), CAST(N'2026-10-16T22:30:03.6000833' AS DateTime2))
INSERT [dbo].[gio_hang] ([ma_gio_hang], [makh], [ngay_tao], [ngay_cap_nhat]) VALUES (2, 3, CAST(N'2026-10-17T11:06:51.8794274' AS DateTime2), CAST(N'2026-10-17T11:55:04.8416637' AS DateTime2))
SET IDENTITY_INSERT [dbo].[gio_hang] OFF
GO

-- Dữ liệu bảng: don_hang (7 dòng)
SET IDENTITY_INSERT [dbo].[don_hang] ON 

INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (1, 1, 1, 1, 3, N'123 Lê Lợi, P. Bến Nghé, Q.1, TP.HCM', N'0988111222', 155000, 22000, 20000, CAST(5.00 AS Decimal(5, 2)), 7750, 147250, N'COD', N'ChuaThanhToan', N'DaGiao', N'GHTK8829102', NULL, NULL, CAST(N'2026-10-16T15:04:53.1590550' AS DateTime2), NULL, CAST(N'2026-10-16T16:00:00.0000000' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (2, 2, 2, 2, 4, N'456 VÃµ VÄƒn NgÃ¢n, TP. Thá»§ Äá»©c, TP.HCM', N'0977333444', 350000, 25000, 40000, CAST(5.00 AS Decimal(5, 2)), 17500, 332500, N'VIETQR', N'DaThanhToan', N'DaHuy', NULL, N'Khach doi y doi sach khac', NULL, CAST(N'2026-10-16T15:04:53.1590550' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (5, 1, 1, 1, NULL, N'1 Vo Van Ngan Thu Duc', N'0987654321', 220000, 22000, 0, CAST(5.00 AS Decimal(5, 2)), 11000, 209000, N'COD', N'ChuaThanhToan', N'DangGiao', N'VNPOST_HN_005', NULL, NULL, CAST(N'2026-10-16T22:30:03.5630683' AS DateTime2), CAST(N'2026-10-16T15:31:18.7525285' AS DateTime2), NULL, NULL, NULL, NULL)
INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (6, 3, 1, 1, NULL, N'462, Linh trung, TP.thủ đức, Hồ Chí minh', N')934567822', 195000, 22000, 0, CAST(5.00 AS Decimal(5, 2)), 9750, 185250, N'COD', N'DaThanhToan', N'HoanTat', N'GHTK-6-62810', NULL, NULL, CAST(N'2026-10-17T11:08:49.4405001' AS DateTime2), CAST(N'2026-10-17T11:09:22.8088482' AS DateTime2), CAST(N'2026-10-17T11:19:28.8038958' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (7, 1, 1, 1, NULL, N'1 Võ Văn Ngân, TP. Thủ Đức', N'0912345678', 480000, 20000, 0, CAST(5.00 AS Decimal(5, 2)), 24000, 456000, N'COD', N'DaThanhToan', N'HoanTat', NULL, NULL, NULL, CAST(N'2026-08-15T10:00:00.0000000' AS DateTime2), NULL, CAST(N'2026-08-18T14:00:00.0000000' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (8, 2, 2, 1, NULL, N'Ký túc xá khu B ĐHQG, Dĩ An', N'0987654321', 620000, 25000, 0, CAST(5.00 AS Decimal(5, 2)), 31000, 589000, N'VNPAY', N'DaThanhToan', N'HoanTat', NULL, NULL, NULL, CAST(N'2026-09-10T11:30:00.0000000' AS DateTime2), NULL, CAST(N'2026-09-13T16:00:00.0000000' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[don_hang] ([madh], [makh], [ma_shop], [ma_nvc], [makm], [dia_chi_giao], [so_dien_thoai_giao], [tong_tien], [phi_van_chuyen], [tien_giam], [chiet_khau_app_phan_tram], [tien_phi_san], [tien_thuc_nhan_shop], [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai], [ma_van_don], [ly_do_huy], [ly_do_tu_choi], [ngay_dat], [ngay_xac_nhan], [ngay_hoan_thanh], [ly_do_tra_hang], [phan_quyet_tranh_chap], [nguoi_xu_ly_tranh_chap]) VALUES (9, 3, 2, 1, NULL, N'462, Linh trung, TP.thủ đức, Hồ Chí minh', N')934567822', 89000, 22000, 0, CAST(5.00 AS Decimal(5, 2)), 4450, 84550, N'COD', N'DaThanhToan', N'DaGiao', N'GHTK-9-65979', NULL, NULL, CAST(N'2026-10-17T11:55:04.7080828' AS DateTime2), CAST(N'2026-10-17T11:56:05.9781648' AS DateTime2), NULL, NULL, NULL, NULL)
SET IDENTITY_INSERT [dbo].[don_hang] OFF
GO

-- Dữ liệu bảng: chi_tiet_don_hang (11 dòng)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (1, 1, 110000, 1)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (1, 3, 65000, 1)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (2, 4, 135000, 1)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (2, 6, 250000, 1)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (5, 1, 110000, 2)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (6, 14, 65000, 3)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (7, 2, 125000, 2)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (7, 10, 95000, 2)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (8, 4, 135000, 2)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (8, 6, 250000, 1)
INSERT [dbo].[chi_tiet_don_hang] ([madh], [masp], [don_gia], [so_luong]) VALUES (9, 13, 89000, 1)
GO

-- Dữ liệu bảng: thanh_toan (3 dòng)
SET IDENTITY_INSERT [dbo].[thanh_toan] ON 

INSERT [dbo].[thanh_toan] ([ma_thanh_toan], [madh], [ma_giao_dich], [phuong_thuc], [so_tien], [noi_dung], [trang_thai], [ngay_thanh_toan]) VALUES (2, 5, NULL, N'COD', 220000, N'Thanh toán đơn hàng #5', N'ChoThanhToan', NULL)
INSERT [dbo].[thanh_toan] ([ma_thanh_toan], [madh], [ma_giao_dich], [phuong_thuc], [so_tien], [noi_dung], [trang_thai], [ngay_thanh_toan]) VALUES (3, 6, NULL, N'COD', 195000, N'Thanh toán đơn hàng #6', N'ChoThanhToan', NULL)
INSERT [dbo].[thanh_toan] ([ma_thanh_toan], [madh], [ma_giao_dich], [phuong_thuc], [so_tien], [noi_dung], [trang_thai], [ngay_thanh_toan]) VALUES (4, 9, NULL, N'COD', 89000, N'Thanh toán đơn hàng #9', N'ChoThanhToan', NULL)
SET IDENTITY_INSERT [dbo].[thanh_toan] OFF
GO

-- Dữ liệu bảng: danh_gia (2 dòng)
SET IDENTITY_INSERT [dbo].[danh_gia] ON 

INSERT [dbo].[danh_gia] ([ma_danh_gia], [madh], [makh], [masp], [so_sao], [noi_dung], [hinh_anh_danh_gia], [trang_thai], [ngay_danh_gia]) VALUES (1, 6, 3, 14, 5, N'Tốt lắm Tốt lắm Tốt lắm Tốt lắm Tốt lắm Tốt lắm Tốt', NULL, N'HienThi', CAST(N'2026-10-17T11:49:02.3558880' AS DateTime2))
INSERT [dbo].[danh_gia] ([ma_danh_gia], [madh], [makh], [masp], [so_sao], [noi_dung], [hinh_anh_danh_gia], [trang_thai], [ngay_danh_gia]) VALUES (2, 9, 3, 13, 5, N'sau khi gửi đánh giá bị lỗi web nè, check kĩ gìsau khi gửi đánh giá bị lỗi web nè, check kĩ gì', NULL, N'HienThi', CAST(N'2026-10-17T11:56:18.5792371' AS DateTime2))
SET IDENTITY_INSERT [dbo].[danh_gia] OFF
GO

-- Dữ liệu bảng: san_pham_yeu_thich (1 dòng)
INSERT [dbo].[san_pham_yeu_thich] ([makh], [masp], [ngay_thich]) VALUES (3, 13, CAST(N'2026-10-17T10:07:52.6125053' AS DateTime2))
GO

-- Dữ liệu bảng: san_pham_da_xem (2 dòng)
INSERT [dbo].[san_pham_da_xem] ([makh], [masp], [thoi_gian_xem]) VALUES (3, 13, CAST(N'2026-10-17T12:04:51.4183590' AS DateTime2))
INSERT [dbo].[san_pham_da_xem] ([makh], [masp], [thoi_gian_xem]) VALUES (3, 14, CAST(N'2026-10-17T12:04:33.5430098' AS DateTime2))
GO

-- Dữ liệu bảng: password_reset_token (1 dòng)
SET IDENTITY_INSERT [dbo].[password_reset_token] ON 

INSERT [dbo].[password_reset_token] ([id], [otp_code], [token], [ma_tai_khoan], [expiry_date], [used], [ngay_tao]) VALUES (1, N'994162', N'994162', 7, CAST(N'2026-10-17T10:10:37.6054155' AS DateTime2), 1, CAST(N'2026-10-17T10:05:37.6054155' AS DateTime2))
SET IDENTITY_INSERT [dbo].[password_reset_token] OFF
GO

-- Dữ liệu bảng: cau_hinh_he_thong (2 dòng)
INSERT [dbo].[cau_hinh_he_thong] ([ma_cau_hinh], [gia_tri], [mo_ta]) VALUES (N'MIN_REVIEW_LENGTH', N'50', N'Số ký tự tối thiểu của bài đánh giá')
INSERT [dbo].[cau_hinh_he_thong] ([ma_cau_hinh], [gia_tri], [mo_ta]) VALUES (N'PHI_SAN_MAC_DINH', N'5.0', N'Tỷ lệ hoa hồng sàn mặc định (%)')
GO

-- Dữ liệu bảng: nhat_ky_bao_mat (1 dòng)
SET IDENTITY_INSERT [dbo].[nhat_ky_bao_mat] ON 

INSERT [dbo].[nhat_ky_bao_mat] ([ma_nhat_ky], [ten_bang], [hanh_dong], [ban_ghi_id], [du_lieu_cu], [du_lieu_moi], [nguoi_thuc_hien], [thoi_gian]) VALUES (1, N'tai_khoan', N'UPDATE', 2, N'VaiTro: NHANVIEN, TrangThai: HoatDong', N'VaiTro: MANAGER, TrangThai: HoatDong', N'WQE-Y7000\Phat', CAST(N'2026-10-17T03:37:19.6242478' AS DateTime2))
SET IDENTITY_INSERT [dbo].[nhat_ky_bao_mat] OFF
GO

-- ====================================================================
-- PHẦN 6: BẢO MẬT & PHÂN QUYỀN (ROLES & USERS)
-- ====================================================================

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'Role_Admin' AND type = 'R') CREATE ROLE Role_Admin;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'Role_Vendor' AND type = 'R') CREATE ROLE Role_Vendor;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'Role_Manager' AND type = 'R') CREATE ROLE Role_Manager;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'Role_KhachHang' AND type = 'R') CREATE ROLE Role_KhachHang;
GO
GRANT CONTROL TO Role_Admin;
GRANT SELECT, INSERT, UPDATE, DELETE ON san_pham TO Role_Vendor;
GRANT SELECT, UPDATE ON don_hang TO Role_Vendor;
GRANT SELECT, UPDATE ON shop TO Role_Manager;
GRANT SELECT ON don_hang TO Role_Manager;
GRANT SELECT ON san_pham TO Role_KhachHang;
GRANT SELECT, INSERT, UPDATE, DELETE ON gio_hang TO Role_KhachHang;
GRANT SELECT, INSERT ON don_hang TO Role_KhachHang;
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'the4book_user')
BEGIN
    CREATE LOGIN [the4book_user] WITH PASSWORD = 'ChangeMe@2026', CHECK_POLICY = OFF;
END
GO
USE [QL_NhaSach];
GO
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'the4book_user')
BEGIN
    CREATE USER [the4book_user] FOR LOGIN [the4book_user];
END
GO
ALTER ROLE [db_owner] ADD MEMBER [the4book_user];
GO

PRINT N'=== THIET LAP CSDL QL_NHASACH TOAN DIEN THANH CONG! ===';
GO
