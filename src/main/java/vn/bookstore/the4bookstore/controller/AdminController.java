package vn.bookstore.the4bookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.DonHang;
import vn.bookstore.the4bookstore.entity.TacGia;
import vn.bookstore.the4bookstore.entity.NhaXuatBan;
import vn.bookstore.the4bookstore.entity.NhaCungCap;
import vn.bookstore.the4bookstore.entity.SanPhamTacGia;
import vn.bookstore.the4bookstore.entity.SanPhamTacGiaId;
import vn.bookstore.the4bookstore.repository.DanhMucRepository;
import vn.bookstore.the4bookstore.repository.NhaCungCapRepository;
import vn.bookstore.the4bookstore.repository.NhaXuatBanRepository;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;
import vn.bookstore.the4bookstore.repository.DonHangRepository;
import vn.bookstore.the4bookstore.repository.TacGiaRepository;
import vn.bookstore.the4bookstore.repository.SanPhamTacGiaRepository;
import vn.bookstore.the4bookstore.service.KhoService;
import vn.bookstore.the4bookstore.service.ReportService;
import vn.bookstore.the4bookstore.service.OrderService;
import vn.bookstore.the4bookstore.service.TacGiaService;
import vn.bookstore.the4bookstore.service.NhaXuatBanService;
import vn.bookstore.the4bookstore.service.SanPhamService;
import vn.bookstore.the4bookstore.service.CloudinaryService;
import vn.bookstore.the4bookstore.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'QUANLY')")
public class AdminController {

    private final SanPhamRepository sanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final TacGiaRepository tacGiaRepository;
    private final SanPhamTacGiaRepository sanPhamTacGiaRepository;

    
    private final ReportService reportService;
    private final OrderService orderService;
    private final DonHangRepository donHangRepository;
    
    @Autowired(required = false)
    private ShopRepository shopRepository;

    @Autowired(required = false)
    private SanPhamService sanPhamService;

    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

        public AdminController(SanPhamRepository sanPhamRepository,
                           DanhMucRepository danhMucRepository,
                           NhaXuatBanRepository nhaXuatBanRepository,
                           NhaCungCapRepository nhaCungCapRepository,
                           TacGiaRepository tacGiaRepository,
                           SanPhamTacGiaRepository sanPhamTacGiaRepository,
                           ReportService reportService,
                           OrderService orderService,
                           DonHangRepository donHangRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.danhMucRepository = danhMucRepository;
        this.nhaXuatBanRepository = nhaXuatBanRepository;
        this.nhaCungCapRepository = nhaCungCapRepository;
        this.tacGiaRepository = tacGiaRepository;
        this.sanPhamTacGiaRepository = sanPhamTacGiaRepository;
        this.reportService = reportService;
        this.orderService = orderService;
        this.donHangRepository = donHangRepository;
    }

    @GetMapping({"", "/"})
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/publishers")
    public String publishers(RedirectAttributes ra) {
        ra.addFlashAttribute("infoMessage", "Quản lý nhà xuất bản đã được tích hợp vào hệ thống.");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("thisMonthRevenue", reportService.getThisMonthRevenue());
        model.addAttribute("monthlyRevenue", reportService.getRevenueByMonth());
        model.addAttribute("topBooks", reportService.getTopSellingBooks());
        model.addAttribute("monthlyOrders", reportService.getThisMonthOrderCount());
        model.addAttribute("todayOrders", reportService.getTodayOrderCount());
        model.addAttribute("totalSold", reportService.getTotalBooksSold());

        long totalBooks = sanPhamRepository.count();
        long lowStock = sanPhamRepository.countLowStock();
        long outOfStock = sanPhamRepository.countOutOfStock();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM, yyyy");
        String formattedDate = LocalDate.now().format(dtf);

        model.addAttribute("totalBooksCount", totalBooks);
        model.addAttribute("lowStockCount", lowStock);
        model.addAttribute("outOfStockCount", outOfStock);
        model.addAttribute("currentDate", formattedDate);

        return "admin/dashboard";
    }

    // ==================== ĐƠN HÀNG ====================
    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status, 
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {
        int pageSize = 10;
        if (page < 1) page = 1;
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, pageSize);
        org.springframework.data.domain.Page<DonHang> orderPage;
        
        if ("pending".equalsIgnoreCase(status) || "ChoXuLy".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiInOrderByNgayDatDesc(
                    java.util.List.of("ChoXuLy", "ChoDuyet", "DonHangMoi"), pageable);
        } else if ("confirmed".equalsIgnoreCase(status) || "DaXacNhan".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiInOrderByNgayDatDesc(
                    java.util.List.of("DaXacNhan", "YeuCauHuy"), pageable);
        } else if ("shipping".equalsIgnoreCase(status) || "DangGiao".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiInOrderByNgayDatDesc(
                    java.util.List.of("DangGiao", "DaLayHang"), pageable);
        } else if ("success".equalsIgnoreCase(status) || "DaGiao".equalsIgnoreCase(status) || "HoanTat".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiInOrderByNgayDatDesc(
                    java.util.List.of("DaGiao", "HoanTat"), pageable);
        } else if ("dispute".equalsIgnoreCase(status) || "TranhChap".equalsIgnoreCase(status) || "TraHangHoanTien".equalsIgnoreCase(status) || "DaHoan".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiInOrderByNgayDatDesc(
                    java.util.List.of("TranhChap", "TraHangHoanTien", "DaHoan"), pageable);
        } else if ("cancelled".equalsIgnoreCase(status) || "DaHuy".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiInOrderByNgayDatDesc(
                    java.util.List.of("DaHuy", "Huy"), pageable);
        } else if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
            orderPage = donHangRepository.findByTrangThaiOrderByNgayDatDesc(status, pageable);
        } else {
            orderPage = donHangRepository.findAllByOrderByNgayDatDesc(pageable);
        }

        long allOrdersCount = donHangRepository.count();
        long pendingCount = donHangRepository.countByTrangThaiIn(java.util.List.of("ChoXuLy", "ChoDuyet", "DonHangMoi"));
        long confirmedCount = donHangRepository.countByTrangThaiIn(java.util.List.of("DaXacNhan", "YeuCauHuy"));
        long shippingCount = donHangRepository.countByTrangThaiIn(java.util.List.of("DangGiao", "DaLayHang"));
        long successCount = donHangRepository.countByTrangThaiIn(java.util.List.of("DaGiao", "HoanTat"));
        long disputeCount = donHangRepository.countByTrangThaiIn(java.util.List.of("TranhChap", "TraHangHoanTien", "DaHoan"));
        long cancelledCount = donHangRepository.countByTrangThaiIn(java.util.List.of("DaHuy", "Huy"));
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        java.time.LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        
        long monthlyOrders = donHangRepository.countByNgayDatBetween(startOfMonth, endOfMonth);
        Long revenueObj = donHangRepository.getRevenueByDateRange(startOfMonth, endOfMonth);
        long monthlyRevenue = revenueObj != null ? revenueObj : 0L;

        int totalPages = orderPage.getTotalPages();
        long totalElements = orderPage.getTotalElements();
        if (page > totalPages && totalPages > 0) {
            return "redirect:/admin/orders?page=" + totalPages + (status != null ? "&status=" + status : "");
        }
        
        long start = totalElements > 0 ? (page - 1) * pageSize + 1 : 0;
        long end = Math.min(page * pageSize, totalElements);

        model.addAttribute("allOrdersCount", allOrdersCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("successCount", successCount);
        model.addAttribute("disputeCount", disputeCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("monthlyOrders", monthlyOrders);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("startCount", start);
        model.addAttribute("endCount", end);
        model.addAttribute("currentStatus", status != null ? status : "");
        
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return donHangRepository.findById(id)
                .map(order -> {
                    model.addAttribute("donHang", order);
                    return "admin/orders-detail";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng với mã: " + id);
                    return "redirect:/admin/orders";
                });
    }

    @PostMapping("/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, 
                                               @RequestParam String status,
                                               @RequestParam(value = "reason", required = false) String reason) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "error", "Đơn hàng thuộc quyền quản lý trực tiếp của Vendor. Admin/Manager chỉ theo dõi thống kê và giải quyết tranh chấp (nếu có), không được tự ý can thiệp trạng thái đơn hàng của gian hàng."
        ));
    }

    @GetMapping("/orders/{id}/json")
    @ResponseBody
    public ResponseEntity<?> getOrderJson(@PathVariable Integer id) {
        return donHangRepository.findById(id)
                .map(dh -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maDH", dh.getMaDH());
                    map.put("trangThai", dh.getTrangThai());
                    map.put("tongTien", dh.getTongTien());
                    map.put("tienGiam", dh.getTienGiam());
                    map.put("ngayDat", dh.getNgayDat() != null ? dh.getNgayDat().toString() : "");
                    map.put("ngayXacNhan", dh.getNgayXacNhan() != null ? dh.getNgayXacNhan().toString() : "");
                    map.put("ngayHoanThanh", dh.getNgayHoanThanh() != null ? dh.getNgayHoanThanh().toString() : "");
                    map.put("diaChiGiao", dh.getDiaChiGiao() != null ? dh.getDiaChiGiao() : "");
                    map.put("soDienThoaiGiao", dh.getSoDienThoaiGiao() != null ? dh.getSoDienThoaiGiao() : "");
                    map.put("khachHang", dh.getKhachHang() != null ? dh.getKhachHang().getHoTen() : "");
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== PHÂN QUYỀN SẢN PHẨM THEO GIAN HÀNG ====================
    // Admin & Manager không CRUD sản phẩm trực tiếp, chuyển hướng đến Quản lý gian hàng
    @GetMapping({"/products", "/books", "/inventory"})
    public String inventory(RedirectAttributes ra) {
        ra.addFlashAttribute("infoMessage", "Sản phẩm được quản trị phân cấp theo từng Gian hàng. Vui lòng chọn gian hàng để theo dõi danh sách sản phẩm.");
        return "redirect:/admin/shops";
    }

    @PostMapping({"/products/save", "/books/save"})
    public String saveProduct(RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Quản trị viên và Quản lý không có quyền thêm hoặc sửa sản phẩm trực tiếp. Mỗi gian hàng tự đăng bán và quản trị sản phẩm của mình qua Kênh Người Bán.");
        return "redirect:/admin/shops";
    }

    @PostMapping({"/products/delete/{id}", "/books/delete/{id}"})
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Quản trị viên và Quản lý không có quyền xóa sản phẩm của gian hàng. Để xử lý vi phạm, vui lòng sử dụng chức năng 'Khóa Sản Phẩm' trong danh sách sản phẩm gian hàng.");
        return "redirect:/admin/shops";
    }

    @GetMapping("/products/{id}/json")
    @ResponseBody
    public ResponseEntity<?> getProductJson(@PathVariable Integer id) {
        return sanPhamRepository.findById(id)
                .map(sp -> {
                    Integer authorId = null;
                    if (sp.getSanPhamTacGias() != null && !sp.getSanPhamTacGias().isEmpty()) {
                        authorId = sp.getSanPhamTacGias().get(0).getTacGia() != null ? sp.getSanPhamTacGias().get(0).getTacGia().getMaTacGia() : null;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("maSP", sp.getMaSP());
                    data.put("tenSP", sp.getTenSP());
                    data.put("loaiSP", sp.getLoaiSP() != null ? sp.getLoaiSP() : "Sach");
                    data.put("maDanhMuc", sp.getDanhMuc() != null && sp.getDanhMuc().getMaDanhMuc() != null ? sp.getDanhMuc().getMaDanhMuc() : 0);
                    data.put("tenDanhMuc", sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : "Chưa phân loại");
                    data.put("maNXB", sp.getNhaXuatBan() != null && sp.getNhaXuatBan().getMaNXB() != null ? sp.getNhaXuatBan().getMaNXB() : 0);
                    data.put("tenNXB", sp.getNhaXuatBan() != null ? sp.getNhaXuatBan().getTenNXB() : "Chưa cập nhật");
                    data.put("maTacGia", authorId != null ? authorId : 0);
                    data.put("tenTacGia", sp.getDanhSachTacGia() != null && !sp.getDanhSachTacGia().isBlank() ? sp.getDanhSachTacGia() : "Chưa cập nhật");
                    data.put("maShop", sp.getShop() != null ? sp.getShop().getMaShop() : 1);
                    data.put("tenShop", sp.getShop() != null ? sp.getShop().getTenShop() : "The4BookStore Official");
                    data.put("hinhAnh", sp.getHinhAnh() != null ? sp.getHinhAnh() : "");
                    data.put("isbn", sp.getISBN() != null ? sp.getISBN() : "");
                    data.put("giaBan", sp.getGiaBan() != null ? sp.getGiaBan() : 0);
                    data.put("soLuongTon", sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0);
                    data.put("soLuongDaBan", sp.getSoLuongDaBan() != null ? sp.getSoLuongDaBan() : 0);
                    data.put("trangThai", sp.getTrangThai() != null ? sp.getTrangThai() : "DangBan");
                    data.put("trangThaiKhoa", sp.getTrangThaiKhoa() != null ? sp.getTrangThaiKhoa() : "BinhThuong");
                    data.put("moTa", sp.getMoTa() != null ? sp.getMoTa() : "");
                    data.put("ngayTao", sp.getNgayTao() != null ? sp.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== QUẢN LÝ DANH MỤC (CRUD) ====================
    @GetMapping("/categories")
    public String categories(Model model) {
        List<DanhMuc> categories = danhMucRepository.findAll();
        Map<Integer, Long> productCounts = new HashMap<>();
        for (DanhMuc dm : categories) {
            productCounts.put(dm.getMaDanhMuc(), sanPhamRepository.countByDanhMuc_MaDanhMuc(dm.getMaDanhMuc()));
        }

        long activeCount = categories.stream().filter(c -> Boolean.TRUE.equals(c.getTrangThai())).count();
        long inactiveCount = categories.size() - activeCount;

        model.addAttribute("categories", categories);
        model.addAttribute("productCounts", productCounts);
        model.addAttribute("totalCategories", categories.size());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);

        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(value = "maDanhMuc", required = false) Integer maDanhMuc,
                               @RequestParam("tenDanhMuc") String tenDanhMuc,
                               @RequestParam(value = "moTa", required = false) String moTa,
                               @RequestParam(value = "trangThai", defaultValue = "true") Boolean trangThai,
                               RedirectAttributes ra) {
        try {
            DanhMuc dm;
            if (maDanhMuc != null && maDanhMuc > 0) {
                dm = danhMucRepository.findById(maDanhMuc).orElse(new DanhMuc());
                ra.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
            } else {
                dm = new DanhMuc();
                ra.addFlashAttribute("successMessage", "Thêm mới danh mục thành công!");
            }
            dm.setTenDanhMuc(tenDanhMuc.trim());
            dm.setMoTa(moTa != null ? moTa.trim() : "");
            dm.setTrangThai(trangThai != null ? trangThai : true);

            danhMucRepository.save(dm);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi lưu danh mục: Tên danh mục có thể đã tồn tại hoặc không hợp lệ!");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes ra) {
        long count = sanPhamRepository.countByDanhMuc_MaDanhMuc(id);
        if (count > 0) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa danh mục này vì đang có " + count + " sản phẩm liên kết!");
            return "redirect:/admin/categories";
        }
        try {
            danhMucRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/toggle/{id}")
    public String toggleCategory(@PathVariable Integer id, RedirectAttributes ra) {
        danhMucRepository.findById(id).ifPresent(dm -> {
            boolean current = Boolean.TRUE.equals(dm.getTrangThai());
            dm.setTrangThai(!current);
            danhMucRepository.save(dm);
            ra.addFlashAttribute("successMessage", "Đã " + (!current ? "kích hoạt" : "tạm khóa") + " danh mục '" + dm.getTenDanhMuc() + "'");
        });
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/json")
    @ResponseBody
    public ResponseEntity<?> getCategoryJson(@PathVariable Integer id) {
        return danhMucRepository.findById(id)
                .map(dm -> ResponseEntity.ok(Map.of(
                        "maDanhMuc", dm.getMaDanhMuc(),
                        "tenDanhMuc", dm.getTenDanhMuc(),
                        "moTa", dm.getMoTa() != null ? dm.getMoTa() : "",
                        "trangThai", Boolean.TRUE.equals(dm.getTrangThai())
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== QUẢN LÝ TÁC GIẢ (CRUD) ====================
    @GetMapping({"/authors", "/tac-gia"})
    public String authors(Model model) {
        List<TacGia> authors = tacGiaRepository.findAll();
        Map<Integer, Long> bookCounts = new HashMap<>();
        for (TacGia tg : authors) {
            bookCounts.put(tg.getMaTacGia(), sanPhamTacGiaRepository.countByTacGia_MaTacGia(tg.getMaTacGia()));
        }
        long authorsWithBooks = bookCounts.values().stream().filter(c -> c > 0).count();

        model.addAttribute("authors", authors);
        model.addAttribute("bookCounts", bookCounts);
        model.addAttribute("totalAuthors", authors.size());
        model.addAttribute("authorsWithBooks", authorsWithBooks);

        return "admin/authors";
    }

    @PostMapping({"/authors/save", "/tac-gia/save"})
    public String saveAuthor(@RequestParam(value = "maTacGia", required = false) Integer maTacGia,
                             @RequestParam("tenTacGia") String tenTacGia,
                             @RequestParam(value = "moTa", required = false) String moTa,
                             RedirectAttributes ra) {
        try {
            TacGia tg;
            if (maTacGia != null && maTacGia > 0) {
                tg = tacGiaRepository.findById(maTacGia).orElse(new TacGia());
                ra.addFlashAttribute("successMessage", "Cập nhật tác giả thành công!");
            } else {
                tg = new TacGia();
                ra.addFlashAttribute("successMessage", "Thêm mới tác giả thành công!");
            }
            tg.setTenTacGia(tenTacGia.trim());
            tg.setMoTa(moTa != null ? moTa.trim() : "");
            tacGiaRepository.save(tg);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi lưu tác giả: " + e.getMessage());
        }
        return "redirect:/admin/authors";
    }

    @PostMapping({"/authors/delete/{id}", "/tac-gia/delete/{id}"})
    public String deleteAuthor(@PathVariable Integer id, RedirectAttributes ra) {
        long count = sanPhamTacGiaRepository.countByTacGia_MaTacGia(id);
        if (count > 0) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa tác giả này vì đang có " + count + " đầu sách liên kết!");
            return "redirect:/admin/authors";
        }
        try {
            tacGiaRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa tác giả thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa tác giả: " + e.getMessage());
        }
        return "redirect:/admin/authors";
    }

    @GetMapping({"/authors/{id}/json", "/tac-gia/{id}/json"})
    @ResponseBody
    public ResponseEntity<?> getAuthorJson(@PathVariable Integer id) {
        return tacGiaRepository.findById(id)
                .map(tg -> ResponseEntity.ok(Map.of(
                        "maTacGia", tg.getMaTacGia(),
                        "tenTacGia", tg.getTenTacGia(),
                        "moTa", tg.getMoTa() != null ? tg.getMoTa() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping({"/authors/{id}/books", "/tac-gia/{id}/books"})
    @ResponseBody
    public ResponseEntity<?> getAuthorBooks(@PathVariable Integer id) {
        List<SanPham> books = sanPhamRepository.findBooksByTacGiaId(id);
        List<Map<String, Object>> bookDtos = books.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maSP", b.getMaSP());
            map.put("tenSP", b.getTenSP());
            map.put("isbn", b.getISBN() != null ? b.getISBN() : "");
            map.put("giaBan", b.getGiaBan());
            map.put("soLuongTon", b.getSoLuongTon());
            map.put("trangThai", b.getTrangThai());
            map.put("hinhAnh", b.getHinhAnh());
            map.put("danhMuc", b.getDanhMuc() != null ? b.getDanhMuc().getTenDanhMuc() : "");
            map.put("nhaXuatBan", b.getNhaXuatBan() != null ? b.getNhaXuatBan().getTenNXB() : "");
            return map;
        }).toList();
        return ResponseEntity.ok(bookDtos);
    }

    // ==================== QUẢN LÝ NHÀ CUNG CẤP ====================
    @GetMapping({"/suppliers", "/nha-cung-cap"})
    public String suppliers(@RequestParam(value = "search", required = false) String search,
                            Model model) {
        List<NhaCungCap> suppliers;
        if (search != null && !search.isBlank()) {
            suppliers = nhaCungCapRepository.findAll().stream()
                    .filter(s -> s.getTenNCC() != null && s.getTenNCC().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            model.addAttribute("searchKeyword", search);
        } else {
            suppliers = nhaCungCapRepository.findAll();
        }
        model.addAttribute("suppliers", suppliers);
        return "admin/suppliers";
    }

    @PostMapping("/suppliers/save")
    public String saveSupplier(@ModelAttribute NhaCungCap supplier, RedirectAttributes ra) {
        try {
            if (supplier.getTrangThai() == null || supplier.getTrangThai().isBlank()) {
                supplier.setTrangThai("HoatDong");
            }
            nhaCungCapRepository.save(supplier);
            ra.addFlashAttribute("successMessage", "Lưu thông tin nhà cung cấp thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/suppliers";
    }

    @PostMapping("/suppliers/delete/{id}")
    public String deleteSupplier(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            nhaCungCapRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa nhà cung cấp thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa nhà cung cấp này vì đã có phiếu nhập/sách liên kết. Bạn có thể chuyển trạng thái sang Tạm ngưng.");
        }
        return "redirect:/admin/suppliers";
    }

    @GetMapping("/api/suppliers/{id}/books")
    @ResponseBody
    public ResponseEntity<?> getSupplierBooks(@PathVariable Integer id) {
        List<SanPham> books = sanPhamRepository.findByNhaCungCapId(id);
        List<Map<String, Object>> result = books.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maSP", b.getMaSP());
            map.put("tenSP", b.getTenSP());
            map.put("isbn", b.getISBN() != null ? b.getISBN() : "");
            map.put("giaBan", b.getGiaBan() != null ? b.getGiaBan() : 0);
            map.put("soLuongTon", b.getSoLuongTon() != null ? b.getSoLuongTon() : 0);
            map.put("hinhAnh", b.getHinhAnh());
            map.put("danhMuc", b.getDanhMuc() != null ? b.getDanhMuc().getTenDanhMuc() : "");
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/suppliers/{id}/available-books")
    @ResponseBody
    public ResponseEntity<?> getAvailableBooksForSupplier(@PathVariable Integer id,
                                                         @RequestParam(value = "keyword", required = false) String keyword) {
        List<SanPham> books = sanPhamRepository.findAvailableBooksForSupplier(id, keyword, PageRequest.of(0, 30));
        List<Map<String, Object>> result = books.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maSP", b.getMaSP());
            map.put("tenSP", b.getTenSP());
            map.put("isbn", b.getISBN() != null ? b.getISBN() : "");
            map.put("giaBan", b.getGiaBan() != null ? b.getGiaBan() : 0);
            map.put("hinhAnh", b.getHinhAnh());
            map.put("nhaCungCap", b.getNhaCungCap() != null ? b.getNhaCungCap().getTenNCC() : "Chưa có NCC");
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/suppliers/{id}/assign-book")
    @ResponseBody
    public ResponseEntity<?> assignBookToSupplier(@PathVariable Integer id, @RequestParam Integer productId) {
        Optional<NhaCungCap> nccOpt = nhaCungCapRepository.findById(id);
        if (nccOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nhà cung cấp không tồn tại"));
        }
        Optional<SanPham> spOpt = sanPhamRepository.findById(productId);
        if (spOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Sách không tồn tại"));
        }
        SanPham sp = spOpt.get();
        sp.setNhaCungCap(nccOpt.get());
        sanPhamRepository.save(sp);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm mặt hàng vào nhà cung cấp!"));
    }

    @PostMapping("/api/suppliers/{id}/unlink-book")
    @ResponseBody
    public ResponseEntity<?> unlinkBookFromSupplier(@PathVariable Integer id, @RequestParam Integer productId) {
        Optional<SanPham> spOpt = sanPhamRepository.findById(productId);
        if (spOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Sách không tồn tại"));
        }
        SanPham sp = spOpt.get();
        if (sp.getNhaCungCap() != null && sp.getNhaCungCap().getMaNCC().equals(id)) {
            sp.setNhaCungCap(null);
            sanPhamRepository.save(sp);
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã gỡ mặt hàng khỏi nhà cung cấp!"));
    }


    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<?> getRecentNotifications() {
        List<DonHang> recentOrders = donHangRepository.findAllByOrderByNgayDatDesc(PageRequest.of(0, 10)).getContent();
        List<Map<String, Object>> notifs = recentOrders.stream().map(dh -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maDH", dh.getMaDH());
            map.put("tenKhachHang", dh.getKhachHang() != null ? dh.getKhachHang().getHoTen() : "Khách vãng lai");
            map.put("tongTien", dh.getTongTien());
            map.put("trangThai", dh.getTrangThai());
            map.put("ngayDat", dh.getNgayDat() != null ? dh.getNgayDat().toString() : "");
            return map;
        }).toList();
        return ResponseEntity.ok(notifs);
    }
}
