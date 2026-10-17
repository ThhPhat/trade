# SƠ ĐỒ QUAN HỆ CƠ SỞ DỮ LIỆU (ERD) — THE4BOOKSTORE
**Hệ thống:** The4BookStore E-Commerce & Marketplace  
**Hệ quản trị CSDL:** Microsoft SQL Server (T-SQL)  
**Mô hình:** Sàn Thương Mại Điện Tử Đa Gian Hàng (Multi-Vendor Marketplace)  
**Phân quyền (4 Roles):** `ADMIN`, `MANAGER`, `VENDOR`, `KHACHHANG`  
**Ghi chú:** Đã loại bỏ hoàn toàn mô hình kho tập trung (`KHO`, `KHO_HANG`, `yeu_cau_cap_hang`, `chi_tiet_cap_hang`) và phân hệ nhân viên cũ (`nhan_vien`). Toàn bộ tồn kho do từng Shop tự quản trị (`san_pham.so_luong_ton`, `phieu_kiem_ke`).

---

```mermaid
erDiagram
    tai_khoan {
        int ma_tai_khoan PK
    }
    khach_hang {
        int makh PK
        int ma_tai_khoan FK
    }
    shop {
        int ma_shop PK
        int ma_tai_khoan FK
    }
    dia_chi_giao_hang {
        int ma_dia_chi PK
        int makh FK
    }
    password_reset_token {
        int id PK
        int ma_tai_khoan FK
    }
    danh_muc {
        int ma_danh_muc PK
    }
    tac_gia {
        int ma_tac_gia PK
    }
    nha_xuat_ban {
        int manxb PK
    }
    san_pham {
        int masp PK
        int ma_shop FK
        int ma_danh_muc FK
    }
    san_pham_tac_gia {
        int masp PK,FK
        int ma_tac_gia PK,FK
    }
    san_pham_yeu_thich {
        int makh PK,FK
        int masp PK,FK
    }
    san_pham_da_xem {
        int makh PK,FK
        int masp PK,FK
    }
    nha_van_chuyen {
        int ma_nvc PK
    }
    khuyen_mai {
        int makm PK
        int ma_shop FK
    }
    voucher_da_luu {
        int id PK
        int makh FK
        int makm FK
    }
    gio_hang {
        int ma_gio_hang PK
        int makh FK
    }
    chi_tiet_gio_hang {
        int ma_chi_tiet PK
        int ma_gio_hang FK
        int masp FK
    }
    don_hang {
        int madh PK
        int makh FK
        int ma_shop FK
        int ma_nvc FK
        int makm FK
        int nguoi_xu_ly_tranh_chap FK
    }
    chi_tiet_don_hang {
        int madh PK,FK
        int masp PK,FK
    }
    thanh_toan {
        int ma_thanh_toan PK
        int madh FK
    }
    hoa_don {
        int ma_hd PK
        int ma_dh FK
    }
    danh_gia {
        int ma_danh_gia PK
        int madh FK
        int makh FK
        int masp FK
    }
    danh_gia_media {
        int ma_media PK
        int ma_danh_gia FK
    }
    phieu_kiem_ke {
        int ma_phieu_kiem_ke PK
        int ma_shop FK
    }
    chi_tiet_kiem_ke {
        int ma_chi_tiet PK
        int ma_phieu_kiem_ke FK
        int masp FK
    }
    cau_hinh_he_thong {
        varchar ma_cau_hinh PK
    }
    nhat_ky_bao_mat {
        int ma_nhat_ky PK
    }

    %% Relationships
    tai_khoan ||--o| khach_hang : "so_huu"
    tai_khoan ||--o| shop : "so_huu"
    tai_khoan ||--o{ password_reset_token : "cap_token"
    tai_khoan ||--o{ don_hang : "xu_ly_tranh_chap"

    khach_hang ||--o{ dia_chi_giao_hang : "co"
    khach_hang ||--o{ gio_hang : "co"
    khach_hang ||--o{ don_hang : "dat"
    khach_hang ||--o{ danh_gia : "viet"
    khach_hang ||--o{ voucher_da_luu : "luu"
    khach_hang ||--o{ san_pham_yeu_thich : "thich"
    khach_hang ||--o{ san_pham_da_xem : "xem"

    shop ||--o{ san_pham : "ban"
    shop ||--o{ don_hang : "nhan"
    shop ||--o{ khuyen_mai : "phat_hanh"
    shop ||--o{ phieu_kiem_ke : "lap"

    danh_muc ||--o{ san_pham : "phan_loai"

    tac_gia ||--o{ san_pham_tac_gia : "dong_tac_gia"
    san_pham ||--o{ san_pham_tac_gia : "thuoc_tac_gia"

    san_pham ||--o{ chi_tiet_gio_hang : "nam_trong"
    gio_hang ||--o{ chi_tiet_gio_hang : "chua"

    san_pham ||--o{ chi_tiet_don_hang : "nam_trong"
    don_hang ||--o{ chi_tiet_don_hang : "chua"

    san_pham ||--o{ chi_tiet_kiem_ke : "duoc_kiem_ke"
    phieu_kiem_ke ||--o{ chi_tiet_kiem_ke : "chua"

    san_pham ||--o{ danh_gia : "duoc_danh_gia"
    san_pham ||--o{ san_pham_yeu_thich : "duoc_thich"
    san_pham ||--o{ san_pham_da_xem : "duoc_xem"

    nha_van_chuyen ||--o{ don_hang : "giao"

    khuyen_mai ||--o{ don_hang : "ap_dung"
    khuyen_mai ||--o{ voucher_da_luu : "thu_thap"

    don_hang ||--o{ thanh_toan : "thanh_toan"
    don_hang ||--o| hoa_don : "xuat"
    don_hang ||--o{ danh_gia : "danh_gia_tu"

    danh_gia ||--o{ danh_gia_media : "dinh_kem"
```
