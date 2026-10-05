package vn.bookstore.the4bookstore.component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final DanhMucRepository danhMucRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final SanPhamRepository sanPhamRepository;
    private final TacGiaRepository tacGiaRepository;
    private final SanPhamTacGiaRepository sanPhamTacGiaRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String mailUsername;

    public DataSeeder(TaiKhoanRepository taiKhoanRepository, 
                      KhachHangRepository khachHangRepository,
                      DanhMucRepository danhMucRepository,
                      NhaXuatBanRepository nhaXuatBanRepository,
                      NhaCungCapRepository nhaCungCapRepository,
                      SanPhamRepository sanPhamRepository,
                      TacGiaRepository tacGiaRepository,
                      SanPhamTacGiaRepository sanPhamTacGiaRepository,
                      PasswordEncoder passwordEncoder,
                      @org.springframework.beans.factory.annotation.Autowired(required = false) org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.danhMucRepository = danhMucRepository;
        this.nhaXuatBanRepository = nhaXuatBanRepository;
        this.nhaCungCapRepository = nhaCungCapRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.tacGiaRepository = tacGiaRepository;
        this.sanPhamTacGiaRepository = sanPhamTacGiaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.execute("ALTER TABLE khach_hang MODIFY so_dien_thoai VARCHAR(20) NULL");
            } catch (Exception ignored) {}
        }

        String defaultPassword = passwordEncoder.encode("123456");

        // 1. Tạo hoặc đảm bảo 3 tài khoản nòng cốt: ADMIN, MANAGER, USER
        createAccountIfNotFound("admin", "ADMIN", defaultPassword, "admin@the4bookstore.vn");
        createAccountIfNotFound("manager", "MANAGER", defaultPassword, "manager@the4bookstore.vn");
        createAccountIfNotFound("user", "USER", defaultPassword, "user@the4bookstore.vn");

        // Luôn đảm bảo mật khẩu 123456 và trạng thái HoatDong
        taiKhoanRepository.findByTenDangNhap("admin").ifPresent(tk -> {
            tk.setMatKhauHash(defaultPassword);
            tk.setVaiTro("ADMIN");
            tk.setTrangThai("HoatDong");
            taiKhoanRepository.save(tk);
        });
        taiKhoanRepository.findByTenDangNhap("manager").ifPresent(tk -> {
            tk.setMatKhauHash(defaultPassword);
            tk.setVaiTro("MANAGER");
            tk.setTrangThai("HoatDong");
            taiKhoanRepository.save(tk);
        });
        taiKhoanRepository.findByTenDangNhap("user").ifPresent(tk -> {
            tk.setMatKhauHash(defaultPassword);
            tk.setVaiTro("USER");
            tk.setTrangThai("HoatDong");
            taiKhoanRepository.save(tk);
        });

        // Đảm bảo mật khẩu 123456 cho các tài khoản Shop (Vendor)
        for (String vendorUser : java.util.List.of("vendor_demo", "vendor_fahasa", "vendor_hoasen")) {
            taiKhoanRepository.findByTenDangNhap(vendorUser).ifPresent(tk -> {
                tk.setMatKhauHash(defaultPassword);
                tk.setVaiTro("VENDOR");
                tk.setTrangThai("HoatDong");
                taiKhoanRepository.save(tk);
            });
        }

        seedBookstoreData();
        seedAuthorsPublishersAndLinks();
        seedStationeryAndGifts();
    }

    private void createAccountIfNotFound(String username, String role, String password, String email) {
        if (taiKhoanRepository.findByTenDangNhap(username).isEmpty()) {
            TaiKhoan tk = new TaiKhoan();
            tk.setTenDangNhap(username);
            tk.setMatKhauHash(password);
            tk.setVaiTro(role);
            tk.setTrangThai("HoatDong");
            tk.setEmail(email != null ? email : username + "@the4bookstore.vn");
            tk = taiKhoanRepository.save(tk);

            if ("USER".equals(role) || "KHACHHANG".equals(role)) {
                if (khachHangRepository.findByTaiKhoan(tk).isEmpty()) {
                    KhachHang kh = new KhachHang();
                    kh.setHoTen("Người Dùng Test");
                    kh.setSoDienThoai(null);
                    kh.setEmail(tk.getEmail());
                    kh.setDiaChi(null);
                    kh.setTaiKhoan(tk);
                    khachHangRepository.save(kh);
                }
            }
        }
    }

    private void seedBookstoreData() {
        if (sanPhamRepository.count() > 0) {
            return;
        }

        // Seed Categories
        DanhMuc catFiction = createCategoryIfNotFound("Tiểu Thuyết & Văn Học", "Tác phẩm tiểu thuyết trong và ngoài nước");
        DanhMuc catSelfHelp = createCategoryIfNotFound("Phát Triển Bản Thân", "Kỹ năng sống, tư duy và phát triển cá nhân");
        DanhMuc catEcon = createCategoryIfNotFound("Kinh Tế & Tài Chính", "Kinh doanh, đầu tư và tâm lý tài chính");
        DanhMuc catHistory = createCategoryIfNotFound("Khoa Học & Lịch Sử", "Lịch sử nhân loại, vũ trụ và khoa học thường thức");
        DanhMuc catSciFi = createCategoryIfNotFound("Khoa Học Viễn Tưởng", "Tiểu thuyết viễn tưởng kinh điển và hiện đại");

        // Seed Publisher & Supplier
        NhaXuatBan nxb = new NhaXuatBan();
        nxb.setTenNXB("Nhà Xuất Bản Hội Nhà Văn");
        nxb.setDiaChi("Hà Nội");
        nxb.setSoDienThoai("0243822222");
        nxb.setEmail("nxbhnv@gmail.com");
        nxb = nhaXuatBanRepository.save(nxb);

        NhaCungCap ncc = new NhaCungCap();
        ncc.setTenNCC("Công ty Sách Phương Nam");
        ncc.setDiaChi("TP.HCM");
        ncc.setSoDienThoai("0283833333");
        ncc.setEmail("contact@phuongnam.com");
        ncc.setTrangThai("HoatDong");
        ncc = nhaCungCapRepository.save(ncc);

        // Seed Books
        createBook("The Midnight Library", "978-0525559474", 189000, 25, catFiction, nxb, ncc,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAJxhxEUoGYZXYaOldMaT7d3dcCRrYvCD-3AWG8q4nOBMKeBIhtPi1dKb9NiDZHTXWMxMULBn3rsm5dU8_IVOjwK8Bnkg8xf-IGsBbsQoLtc5D6EWqcU1KfQ1LzI9jMW8JHZBBy9sHF_7t1onS8pagaubUOfOqYifnPkQMRgKeMgWgWQ71XMqmHdC9fJzuMiB3aqbLZt8bE5qj4HVtt62KzVUKztkjut685dS6dOCMbTJ_WPnz-9N7y");

        createBook("Atomic Habits - Thay Đổi Tí Hon", "978-0735211292", 220000, 0, catSelfHelp, nxb, ncc,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuC0ZepDd_foKXu3i6ER1m8WWEdur0sxuw_PzY9MNfI4REnQ3rf6R_2Cm9EnfFAhXCPxQTTtVl4s2CrHSmtbVz08lHsAHDSiErKboS8Y4p5NQiHrun0Ek56kgIt1soK3hBbriVWEntfW_SHfXzkp7nmY2OyfVyfhAuJ50SpGyqZseN_277pw-RUdZgMOpRkN3ksmBAnBDCRnpGWTd3UWhZiP2GhZTpYd0mZ4A0F2WZ20z_Y2aCKwxl9w");

        createBook("Tâm Lý Học Về Tiền", "978-0857197689", 150000, 3, catEcon, nxb, ncc,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAUSZ5dqoRMUTssXIap1lFtbGqNj76EO4_JpeLICxgK6i8RQ-b6FBevsmTpBNYLqOGOhEJmM72f7BeRd_P6wYVJ0WrTJJCFwjmrX7y4IYbT9cGZhUYpfZ41NtYpK8wDRUwUU5JolPlF4JYof0nf-Mnjr2hBZFwxagtPhNGSVfaR65hlzsL-oDdP7Q53Ww7C4vzv_B90IFfI-_FjyHy7V4wP5GHZfTpmShIiBp4oEL19xwu0tRaTGEgW");

        createBook("Sapiens: Lược Sử Loài Người", "978-0062316097", 265000, 38, catHistory, nxb, ncc,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCG70bn_Qqg9AKCp8J9XwZoQ0H8otME2a1VxaIQ9mbLAQk60DgJssT5k7D_fsGf6ttO0j-TROb1NzE4QR1_sbRLD856RV8UIFgynUd7SujhVFdhLBJkAPHxPynAmPFKFoKuLSY2tyJX9T_1SDen13Mk4MN2urDBWhtknhDsTK8-O2oLxA8yjhXNZy0YUWK2U5gHXvRhAIj2d-YivtCOD_hFsb8Qu8Z00zBpmbfcRC4at7Dd35UbsRB0");

        createBook("Cây Cam Ngọt Của Tôi", "978-6047781234", 108000, 45, catFiction, nxb, ncc,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAJxhxEUoGYZXYaOldMaT7d3dcCRrYvCD-3AWG8q4nOBMKeBIhtPi1dKb9NiDZHTXWMxMULBn3rsm5dU8_IVOjwK8Bnkg8xf-IGsBbsQoLtc5D6EWqcU1KfQ1LzI9jMW8JHZBBy9sHF_7t1onS8pagaubUOfOqYifnPkQMRgKeMgWgWQ71XMqmHdC9fJzuMiB3aqbLZt8bE5qj4HVtt62KzVUKztkjut685dS6dOCMbTJ_WPnz-9N7y");

        createBook("Dune - Xứ Cát", "978-0441013593", 245000, 18, catSciFi, nxb, ncc,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCG70bn_Qqg9AKCp8J9XwZoQ0H8otME2a1VxaIQ9mbLAQk60DgJssT5k7D_fsGf6ttO0j-TROb1NzE4QR1_sbRLD856RV8UIFgynUd7SujhVFdhLBJkAPHxPynAmPFKFoKuLSY2tyJX9T_1SDen13Mk4MN2urDBWhtknhDsTK8-O2oLxA8yjhXNZy0YUWK2U5gHXvRhAIj2d-YivtCOD_hFsb8Qu8Z00zBpmbfcRC4at7Dd35UbsRB0");
    }

    private DanhMuc createCategoryIfNotFound(String name, String desc) {
        return danhMucRepository.findAll().stream()
                .filter(d -> d.getTenDanhMuc().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    DanhMuc dm = new DanhMuc();
                    dm.setTenDanhMuc(name);
                    dm.setMoTa(desc);
                    dm.setTrangThai(true);
                    return danhMucRepository.save(dm);
                });
    }

    private void createBook(String title, String isbn, int price, int stock, DanhMuc category, NhaXuatBan nxb, NhaCungCap ncc, String coverUrl) {
        SanPham sp = new SanPham();
        sp.setTenSP(title);
        sp.setISBN(isbn);
        sp.setLoaiSP("Sach");
        sp.setGiaBan(price);
        sp.setSoLuongTon(stock);
        sp.setMucTonToiThieu(5);
        sp.setDanhMuc(category);
        sp.setNhaXuatBan(nxb);
        sp.setNhaCungCap(ncc);
        sp.setMoTa(coverUrl);
        sp.setTrangThai(stock > 0 ? "DangBan" : "HetHang");
        sp.setNgayTao(LocalDateTime.now());
        sanPhamRepository.save(sp);
    }

    private void seedStationeryAndGifts() {
        // Danh mục của Văn phòng phẩm
        DanhMuc catSoTay = createCategoryIfNotFound("Sổ Tay", "Sổ tay da, sổ ghi chép, sổ kế hoạch");
        DanhMuc catBut = createCategoryIfNotFound("Bút", "Các loại bút viết, bút máy, bút bi cao cấp");
        DanhMuc catDungCu = createCategoryIfNotFound("Dụng Cụ Học Tập & Làm Việc", "Kẹp sách, đèn đọc sách, thước kẻ và phụ kiện");

        // Danh mục của Quà tặng & Trang trí
        DanhMuc catTuiTote = createCategoryIfNotFound("Túi Tote", "Túi vải canvas thời trang phong cách mọt sách");
        DanhMuc catBookNook = createCategoryIfNotFound("Mô Hình Book Nook", "Mô hình gỗ 3D Book Nook trang trí giá sách");
        DanhMuc catBoardgame = createCategoryIfNotFound("Boardgame & Quà Tặng", "Trò chơi boardgame trí tuệ và hộp quà tặng độc đáo");

        NhaCungCap ncc = nhaCungCapRepository.findAll().stream().findFirst().orElseGet(() -> {
            NhaCungCap n = new NhaCungCap();
            n.setTenNCC("Công ty Sách Phương Nam");
            n.setDiaChi("TP.HCM");
            n.setSoDienThoai("0283833333");
            n.setEmail("contact@phuongnam.com");
            n.setTrangThai("HoatDong");
            return nhaCungCapRepository.save(n);
        });

        createProductItem("Sổ Tay Bìa Da Vintage Classic", "VPP-001", "VanPhongPham", 85000, 40, catSoTay, ncc,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80");
        createProductItem("Bút Máy Thư Pháp Pilot Kakuno", "VPP-002", "VanPhongPham", 210000, 25, catBut, ncc,
                "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500&auto=format&fit=crop&q=80");
        createProductItem("Bộ Bookmark Kim Loại Mạ Vàng Cổ Điển", "VPP-003", "VanPhongPham", 45000, 60, catDungCu, ncc,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&auto=format&fit=crop&q=80");
        createProductItem("Đèn Kẹp Đọc Sách Bảo Vệ Mắt LED", "VPP-004", "VanPhongPham", 135000, 15, catDungCu, ncc,
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&auto=format&fit=crop&q=80");

        createProductItem("Túi Canvas The4BookStore Vintage Tote", "QT-001", "QuaTang", 120000, 35, catTuiTote, ncc,
                "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&auto=format&fit=crop&q=80");
        createProductItem("Mô Hình Book Nook Gỗ 3D Hẻm Xéo", "QT-002", "QuaTang", 450000, 10, catBookNook, ncc,
                "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&auto=format&fit=crop&q=80");
        createProductItem("Bộ Boardgame Catan Bản Tiếng Việt", "QT-003", "QuaTang", 590000, 8, catBoardgame, ncc,
                "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=500&auto=format&fit=crop&q=80");
        createProductItem("Hộp Quà Tặng Người Yêu Sách Reader Box", "QT-004", "QuaTang", 320000, 18, catBoardgame, ncc,
                "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=500&auto=format&fit=crop&q=80");
    }

    private void createProductItem(String title, String isbn, String loaiSP, int price, int stock, DanhMuc category, NhaCungCap ncc, String coverUrl) {
        if (sanPhamRepository.findAll().stream().anyMatch(p -> isbn.equalsIgnoreCase(p.getISBN()))) {
            return;
        }
        SanPham sp = new SanPham();
        sp.setTenSP(title);
        sp.setISBN(isbn);
        sp.setLoaiSP(loaiSP);
        sp.setGiaBan(price);
        sp.setSoLuongTon(stock);
        sp.setMucTonToiThieu(5);
        sp.setDanhMuc(category);
        sp.setNhaCungCap(ncc);
        sp.setMoTa(coverUrl);
        sp.setTrangThai(stock > 0 ? "DangBan" : "HetHang");
        sp.setNgayTao(LocalDateTime.now());
        sanPhamRepository.save(sp);
    }

    private void seedAuthorsPublishersAndLinks() {
        // 1. Seed Nhà Xuất Bản
        NhaXuatBan nxbHoiNhaVan = createPublisherIfNotFound("Nhà Xuất Bản Hội Nhà Văn", "Hà Nội", "0243822222", "nxbhnv@gmail.com");
        NhaXuatBan nxbTre = createPublisherIfNotFound("Nhà Xuất Bản Trẻ", "TP. Hồ Chí Minh", "02839316289", "hopthu@nxbtre.com.vn");
        NhaXuatBan nxbKimDong = createPublisherIfNotFound("Nhà Xuất Bản Kim Đồng", "Hà Nội", "02439434730", "cskh_online@nxbkimdong.com.vn");
        NhaXuatBan nxbTheGioi = createPublisherIfNotFound("Nhà Xuất Bản Thế Giới", "Hà Nội", "02438253841", "thegioi@hn.vnn.vn");
        NhaXuatBan nxbPhuNu = createPublisherIfNotFound("Nhà Xuất Bản Phụ Nữ Việt Nam", "Hà Nội", "02439710723", "phunuvn@gmail.com");

        // 2. Seed Tác Giả
        TacGia tgMattHaig = createAuthorIfNotFound("Matt Haig", "Tiểu thuyết gia và nhà báo nổi tiếng người Anh, tác giả cuốn sách best-seller quốc tế The Midnight Library.");
        TacGia tgJamesClear = createAuthorIfNotFound("James Clear", "Chuyên gia hàng đầu thế giới về hình thành thói quen và tối ưu hóa năng suất cá nhân, tác giả Atomic Habits.");
        TacGia tgMorganHousel = createAuthorIfNotFound("Morgan Housel", "Đối tác tại The Collaborative Fund, cựu nhà phân tích tài chính tại The Motley Fool và The Wall Street Journal, tác giả Tâm Lý Học Về Tiền.");
        TacGia tgYuval = createAuthorIfNotFound("Yuval Noah Harari", "Giáo sư khoa Lịch sử tại Đại học Hebrew Jerusalem, tác giả bộ sách Sapiens Lược sử loài người kinh điển.");
        TacGia tgJose = createAuthorIfNotFound("José Mauro de Vasconcelos", "Nhà văn lỗi lạc người Brazil, tác giả kiệt tác văn học kinh điển Cây Cam Ngọt Của Tôi.");
        TacGia tgFrank = createAuthorIfNotFound("Frank Herbert", "Đại văn hào người Mỹ, tác giả thiên sử thi khoa học viễn tưởng Dune (Xứ Cát).");
        createAuthorIfNotFound("Nguyễn Nhật Ánh", "Nhà văn nổi tiếng của bao thế hệ độc giả Việt Nam với các tác phẩm như Cho Tôi Xin Một Vé Đi Tuổi Thơ, Mắt Biếc.");
        createAuthorIfNotFound("Rosie Nguyễn", "Tác giả của cuốn sách bán chạy Tuổi Trẻ Đáng Giá Bao Nhiêu, blogger và người truyền cảm hứng sống.");

        // 3. Liên kết Sách với Tác giả và NXB
        linkBookAuthorAndPublisher("The Midnight Library", tgMattHaig, nxbHoiNhaVan);
        linkBookAuthorAndPublisher("Atomic Habits - Thay Đổi Tí Hon", tgJamesClear, nxbTheGioi);
        linkBookAuthorAndPublisher("Tâm Lý Học Về Tiền", tgMorganHousel, nxbTre);
        linkBookAuthorAndPublisher("Sapiens: Lược Sử Loài Người", tgYuval, nxbTheGioi);
        linkBookAuthorAndPublisher("Cây Cam Ngọt Của Tôi", tgJose, nxbHoiNhaVan);
        linkBookAuthorAndPublisher("Dune - Xứ Cát", tgFrank, nxbHoiNhaVan);
    }

    private void linkBookAuthorAndPublisher(String bookTitle, TacGia author, NhaXuatBan publisher) {
        sanPhamRepository.findAll().stream()
                .filter(sp -> sp.getTenSP() != null && sp.getTenSP().equalsIgnoreCase(bookTitle))
                .findFirst()
                .ifPresent(sp -> {
                    if (publisher != null && sp.getNhaXuatBan() == null) {
                        sp.setNhaXuatBan(publisher);
                        sanPhamRepository.save(sp);
                    }
                    if (author != null && sanPhamTacGiaRepository.findBySanPham_MaSP(sp.getMaSP()).isEmpty()) {
                        SanPhamTacGia sptg = new SanPhamTacGia(sp, author, 1);
                        sanPhamTacGiaRepository.save(sptg);
                    }
                });
    }

    private NhaXuatBan createPublisherIfNotFound(String name, String address, String phone, String email) {
        return nhaXuatBanRepository.findAll().stream()
                .filter(n -> n.getTenNXB().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    NhaXuatBan nxb = new NhaXuatBan();
                    nxb.setTenNXB(name);
                    nxb.setDiaChi(address);
                    nxb.setSoDienThoai(phone);
                    nxb.setEmail(email);
                    return nhaXuatBanRepository.save(nxb);
                });
    }

    private TacGia createAuthorIfNotFound(String name, String bio) {
        return tacGiaRepository.findAll().stream()
                .filter(t -> t.getTenTacGia().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    TacGia tg = new TacGia();
                    tg.setTenTacGia(name);
                    tg.setMoTa(bio);
                    return tacGiaRepository.save(tg);
                });
    }
}
