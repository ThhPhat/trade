package vn.bookstore.the4bookstore.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.entity.DonHang;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.repository.DanhMucRepository;
import vn.bookstore.the4bookstore.repository.DonHangRepository;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;
import vn.bookstore.the4bookstore.service.KhuyenMaiService;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping({"/admin/vouchers", "/admin/promotions"})
public class AdminPromotionController {

    private final KhuyenMaiService khuyenMaiService;
    private final DanhMucRepository danhMucRepository;
    
    private final DonHangRepository donHangRepository;

    public AdminPromotionController(KhuyenMaiService khuyenMaiService,
                                    DanhMucRepository danhMucRepository,
                                    
                                    DonHangRepository donHangRepository) {
        this.khuyenMaiService = khuyenMaiService;
        this.danhMucRepository = danhMucRepository;
        
        this.donHangRepository = donHangRepository;
    }

    // 1. Danh sách Voucher & Thống kê tổng quan (Bento Data Table)
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loaiGiam,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            Model model) {

        List<KhuyenMai> promotions = khuyenMaiService.getAllPromotions(keyword, loaiGiam, trangThai, tuNgay, denNgay);
        List<KhuyenMai> allPromotions = khuyenMaiService.getAllPromotions(null, null, null, null, null);

        // Bento Stats
        long totalCount = allPromotions.size();
        long activeCount = allPromotions.stream().filter(km -> "DangDienRa".equals(km.getComputedStatus())).count();
        long totalUsedCount = allPromotions.stream()
                .mapToLong(km -> km.getSoLuongDaDung() != null ? km.getSoLuongDaDung() : 0)
                .sum();
        
        long totalDiscountGiven = 0;
        for (KhuyenMai km : allPromotions) {
            Long discount = donHangRepository.sumTienGiamByKhuyenMai(km);
            if (discount != null) {
                totalDiscountGiven += discount;
            }
        }

        model.addAttribute("promotions", promotions);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("totalUsedCount", totalUsedCount);
        model.addAttribute("totalDiscountGiven", totalDiscountGiven);

        // Filter attributes
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiGiam", loaiGiam);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        return "admin/promotions/index";
    }

    // 2. Form Thêm mới (4 Khối)
    @GetMapping("/them")
    public String createForm(Model model) {
        KhuyenMai km = new KhuyenMai();
        km.setNgayBatDau(LocalDateTime.now().withSecond(0).withNano(0));
        km.setNgayKetThuc(LocalDateTime.now().plusMonths(1).withSecond(0).withNano(0));
        km.setLoaiGiam("PhanTram");
        km.setGiaTriGiam(10);
        km.setGioiHanMoiKhachHang(1);
        km.setHienThiCongKhai(true);
        km.setApDungCho("ALL");
        km.setDoiTuongKhachHang("ALL");
        km.setTrangThai("HoatDong");

        model.addAttribute("khuyenMai", km);
        model.addAttribute("isEdit", false);
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        return "admin/promotions/form";
    }

    // 3. Form Chỉnh sửa
    @GetMapping("/{id}/sua")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<KhuyenMai> kmOpt = khuyenMaiService.getById(id);
        if (kmOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã khuyến mãi #" + id);
            return "redirect:/admin/vouchers";
        }

        model.addAttribute("khuyenMai", kmOpt.get());
        model.addAttribute("isEdit", true);
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        return "admin/promotions/form";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // 4. Lưu Khuyến mãi (Tạo mới hoặc Cập nhật)
    @PostMapping("/luu")
    public String save(@ModelAttribute("khuyenMai") KhuyenMai khuyenMai,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getFieldErrors().forEach(err -> {
                sb.append("Trường '").append(err.getField()).append("': ").append(err.getDefaultMessage()).append("; ");
            });
            model.addAttribute("errorMessage", "Dữ liệu nhập không hợp lệ: " + (sb.length() > 0 ? sb.toString() : "Vui lòng kiểm tra lại thông tin ngày giờ hoặc các trường số."));
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null && khuyenMai.getMaKM() > 0);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }

        if (khuyenMai.getMaKM() != null && khuyenMai.getMaKM() <= 0) {
            khuyenMai.setMaKM(null);
        }

        // Validate cơ bản
        if (khuyenMai.getMaCode() == null || khuyenMai.getMaCode().trim().isEmpty()) {
            khuyenMai.setMaCode(KhuyenMaiService.generateRandomCode("KM", 8));
        } else {
            khuyenMai.setMaCode(khuyenMai.getMaCode().trim().toUpperCase());
        }

        if (khuyenMai.getTenKM() == null || khuyenMai.getTenKM().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập tên chương trình khuyến mãi!");
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }

        if (!khuyenMaiService.isCodeUnique(khuyenMai.getMaCode(), khuyenMai.getMaKM())) {
            model.addAttribute("errorMessage", "Mã khuyến mãi [" + khuyenMai.getMaCode() + "] đã tồn tại! Vui lòng chọn mã khác.");
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }

        if (khuyenMai.getNgayBatDau() == null) {
            khuyenMai.setNgayBatDau(LocalDateTime.now().withSecond(0).withNano(0));
        }
        if (khuyenMai.getNgayKetThuc() == null) {
            khuyenMai.setNgayKetThuc(LocalDateTime.now().plusMonths(1).withSecond(0).withNano(0));
        }

        if (khuyenMai.getNgayKetThuc().isBefore(khuyenMai.getNgayBatDau())) {
            model.addAttribute("errorMessage", "Thời gian kết thúc phải diễn ra sau thời gian bắt đầu!");
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }

        if (khuyenMai.getGiaTriGiam() == null || khuyenMai.getGiaTriGiam() <= 0) {
            model.addAttribute("errorMessage", "Giá trị giảm phải lớn hơn 0!");
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }

        if ("PhanTram".equalsIgnoreCase(khuyenMai.getLoaiGiam()) && khuyenMai.getGiaTriGiam() > 100) {
            model.addAttribute("errorMessage", "Giảm theo phần trăm không được vượt quá 100%!");
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }

        if (khuyenMai.getMaKM() != null) {
            khuyenMaiService.getById(khuyenMai.getMaKM()).ifPresent(existing -> {
                if (khuyenMai.getSoLuongDaDung() == null) {
                    khuyenMai.setSoLuongDaDung(existing.getSoLuongDaDung());
                }
            });
        }
        if (khuyenMai.getSoLuongDaDung() == null) {
            khuyenMai.setSoLuongDaDung(0);
        }
        if (khuyenMai.getHienThiCongKhai() == null) {
            khuyenMai.setHienThiCongKhai(false);
        }
        if (khuyenMai.getGioiHanMoiKhachHang() == null || khuyenMai.getGioiHanMoiKhachHang() <= 0) {
            khuyenMai.setGioiHanMoiKhachHang(1);
        }
        if (khuyenMai.getTrangThai() == null || khuyenMai.getTrangThai().trim().isEmpty()) {
            khuyenMai.setTrangThai("HoatDong");
        }
        if (khuyenMai.getApDungCho() == null) {
            khuyenMai.setApDungCho("ALL");
        }
        if (khuyenMai.getDoiTuongKhachHang() == null) {
            khuyenMai.setDoiTuongKhachHang("ALL");
        }

        try {
            boolean isNew = (khuyenMai.getMaKM() == null);
            khuyenMaiService.save(khuyenMai);

            redirectAttributes.addFlashAttribute("successMessage",
                    (isNew ? "Tạo mới" : "Cập nhật") + " mã khuyến mãi [" + khuyenMai.getMaCode() + "] thành công!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi lưu mã khuyến mãi: " + e.getMessage());
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("isEdit", khuyenMai.getMaKM() != null);
            model.addAttribute("danhMucs", danhMucRepository.findAll());
            return "admin/promotions/form";
        }
    }

    // 5. Thao tác nhanh: Toggle Bật / Tắt trạng thái
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            khuyenMaiService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái mã khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // 6. Thao tác nhanh: Nhân bản mã (Duplicate)
    @PostMapping("/{id}/duplicate")
    public String duplicate(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            KhuyenMai cloned = khuyenMaiService.duplicate(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Nhân bản mã thành công! Đã tạo mã mới [" + cloned.getMaCode() + "].");
            return "redirect:/admin/vouchers/" + cloned.getMaKM() + "/sua";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể nhân bản: " + e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }

    // 7. Thao tác nhanh: Xóa mã
    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            khuyenMaiService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mã khuyến mãi thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa mã: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // 8. Trang Chi tiết Hiệu quả & Đối soát (`/admin/vouchers/{id}`)
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<KhuyenMai> kmOpt = khuyenMaiService.getById(id);
        if (kmOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã khuyến mãi #" + id);
            return "redirect:/admin/vouchers";
        }

        KhuyenMai km = kmOpt.get();
        Long totalOrders = khuyenMaiService.countOrdersByKhuyenMai(km);
        Long totalDiscount = khuyenMaiService.sumDiscountByKhuyenMai(km);
        Long totalRevenue = khuyenMaiService.sumRevenueByKhuyenMai(km);
        List<DonHang> orders = khuyenMaiService.getOrdersByKhuyenMai(km);

        double averageOrderValue = (totalOrders != null && totalOrders > 0 && totalRevenue != null)
                ? (double) totalRevenue / totalOrders : 0;

        model.addAttribute("khuyenMai", km);
        model.addAttribute("totalOrders", totalOrders != null ? totalOrders : 0);
        model.addAttribute("totalDiscount", totalDiscount != null ? totalDiscount : 0);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        model.addAttribute("averageOrderValue", averageOrderValue);
        model.addAttribute("orders", orders);

        return "admin/promotions/detail";
    }

    // 9. Sinh mã hàng loạt (Batch Generation)
    @PostMapping("/batch")
    public String generateBatch(
            @RequestParam(defaultValue = "SALE") String prefix,
            @RequestParam(defaultValue = "10") int quantity,
            @RequestParam String tenKM,
            @RequestParam(required = false) String moTa,
            @RequestParam(defaultValue = "PhanTram") String loaiGiam,
            @RequestParam(defaultValue = "10") Integer giaTriGiam,
            @RequestParam(required = false) Integer giamToiDa,
            @RequestParam(required = false) Integer donToiThieu,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            RedirectAttributes redirectAttributes) {

        try {
            KhuyenMai template = new KhuyenMai();
            template.setTenKM(tenKM);
            template.setMoTa(moTa);
            template.setLoaiGiam(loaiGiam);
            template.setGiaTriGiam(giaTriGiam);
            template.setGiamToiDa(giamToiDa);
            template.setDonToiThieu(donToiThieu);
            template.setSoLuongToiDa(1); // Mỗi mã 1 lần dùng
            template.setNgayBatDau(ngayBatDau != null ? ngayBatDau : LocalDateTime.now());
            template.setNgayKetThuc(ngayKetThuc != null ? ngayKetThuc : LocalDateTime.now().plusMonths(1));
            template.setHienThiCongKhai(false); // Mã batch thường là mã phát riêng
            template.setApDungCho("ALL");
            template.setDoiTuongKhachHang("ALL");

            List<KhuyenMai> created = khuyenMaiService.generateBatch(prefix, quantity, template);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã sinh thành công " + created.size() + " mã khuyến mãi theo tiền tố [" + prefix.toUpperCase() + "]!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo mã hàng loạt: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // 10. Xuất file CSV
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loaiGiam,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        List<KhuyenMai> promotions = khuyenMaiService.getAllPromotions(keyword, loaiGiam, trangThai, tuNgay, denNgay);
        byte[] csvData = khuyenMaiService.exportToCsv(promotions);

        String filename = "vouchers_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
