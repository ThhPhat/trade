# HƯỚNG DẪN THIẾT LẬP CƠ SỞ DỮ LIỆU QL_NHASACH
**Hệ thống:** The4BookStore E-Commerce & Marketplace  
**Hệ quản trị CSDL:** Microsoft SQL Server 2022/2025 (T-SQL)  
**Ngày cập nhật:** 17/10/2026  

---

## 1. CÁC TẬP TIN CƠ SỞ DỮ LIỆU
* **`QL_NhaSach_Full_Setup.sql`**: Tập tin script SQL duy nhất và hoàn chỉnh nhất của dự án. Bao gồm toàn bộ cấu trúc bảng (3NF), khóa chính/ngoại, ràng buộc, chỉ mục, View, Trigger, Function, Stored Procedure, phân quyền 4 Roles và toàn bộ dữ liệu mẫu (mật khẩu tất cả tài khoản là `123456`).
* **`QL_NhaSach.bak`**: File sao lưu (Backup) nguyên trạng từ hệ thống live.
* **`restore_sqlserver.bat`**: File chạy tự động 1-click để phục hồi CSDL và tạo user `the4book_user`.
* **`restore_sqlserver.ps1`**: Script PowerShell tự động hóa tiến trình phục hồi.

---

## 2. HƯỚNG DẪN CÀI ĐẶT CSDL

### Cách 1: Nhanh nhất (Tự động 1-Click bằng file .bat)
1. Đảm bảo dịch vụ SQL Server (MSSQLSERVER hoặc SQLEXPRESS) đang chạy.
2. Click đúp chuột vào file **`restore_sqlserver.bat`**.
3. Tiến trình sẽ tự động:
   - Phục hồi CSDL `QL_NhaSach` từ `QL_NhaSach.bak`.
   - Tạo Login và User `the4book_user` với mật khẩu `ChangeMe@2026` và gán quyền `db_owner`.
   - Tự động tạo file `.env` nếu dự án chưa có.

### Cách 2: Chạy trực tiếp script SQL trong SSMS
1. Mở **SQL Server Management Studio (SSMS)** và kết nối tới máy chủ SQL Server của bạn.
2. Mở file **`QL_NhaSach_Full_Setup.sql`**.
3. Nhấn **Execute (F5)** để khởi tạo toàn bộ CSDL và dữ liệu mẫu.

---

## 3. TÀI KHOẢN TRUY CẬP HỆ THỐNG
Tất cả các tài khoản mặc định đều có mật khẩu: **`123456`**

| Vai trò | Email đăng nhập | Tên người dùng |
| :--- | :--- | :--- |
| **Admin** | `admin@the4bookstore.vn` | Trần Quản Trị |
| **Manager** | `manager1@the4bookstore.vn` | Lê Thị Quản Lý |
| **Vendor** | `shop_fahasa@gmail.com` | Fahasa Official Store |
| **Vendor** | `nhanam@gmail.com` | Nhã Nam Book Store |
| **Khách hàng** | `tran.van.b@gmail.com` | Trần Văn Bình |
| **Khách hàng** | `nguyenvanan@gmail.com` | Nguyễn Văn An |
