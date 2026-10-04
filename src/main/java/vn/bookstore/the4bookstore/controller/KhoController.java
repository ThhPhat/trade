package vn.bookstore.the4bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.bookstore.the4bookstore.dto.CreatePhieuKeRequest;
import vn.bookstore.the4bookstore.dto.CreatePhieuNhapRequest;
import vn.bookstore.the4bookstore.repository.NhaCungCapRepository;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;
import vn.bookstore.the4bookstore.service.KhoService;

import java.util.Map;

@Controller
@RequestMapping("/admin/kho")
public class KhoController {

    private final KhoService khoService;
    private final SanPhamRepository sachRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final vn.bookstore.the4bookstore.repository.NhanVienRepository nhanVienRepository;

    private final vn.bookstore.the4bookstore.repository.KhoHangRepository khoHangRepository;

    public KhoController(KhoService khoService,
                         SanPhamRepository sachRepository,
                         NhaCungCapRepository nhaCungCapRepository,
                         vn.bookstore.the4bookstore.repository.NhanVienRepository nhanVienRepository,
                         vn.bookstore.the4bookstore.repository.KhoHangRepository khoHangRepository) {
        this.khoService = khoService;
        this.sachRepository = sachRepository;
        this.nhaCungCapRepository = nhaCungCapRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.khoHangRepository = khoHangRepository;
    }

    @GetMapping
    public String listKho(Model model) {
        model.addAttribute("khos", khoService.getAllKho());
        return "admin/kho/list";
    }

    @GetMapping("/{id}/detail")
    public String khoDetail(@PathVariable Integer id, Model model) {
        vn.bookstore.the4bookstore.entity.Kho kho = khoService.getKhoById(id);
        model.addAttribute("kho", kho);
        model.addAttribute("khoHangs", khoHangRepository.findByKho(kho));
        return "admin/kho/detail";
    }

    @PostMapping("/save")
    public String saveKho(@RequestParam(required = false) Integer maKho, 
                          @RequestParam String tenKho, 
                          @RequestParam String diaChi,
                          @RequestParam(required = false) String soDienThoai,
                          @RequestParam(required = false) String ghiChu) {
        vn.bookstore.the4bookstore.entity.Kho k = new vn.bookstore.the4bookstore.entity.Kho();
        k.setTenKho(tenKho);
        k.setDiaChi(diaChi);
        k.setSoDienThoai(soDienThoai);
        k.setGhiChu(ghiChu);
        if (maKho != null) {
            khoService.updateKho(maKho, k);
        } else {
            khoService.createKho(k);
        }
        return "redirect:/admin/kho";
    }

    @PostMapping("/delete/{id}")
    public String deleteKho(@PathVariable Integer id) {
        try {
            khoService.deleteKho(id);
        } catch (Exception e) {}
        return "redirect:/admin/kho";
    }

    @GetMapping("/phieu-nhap")
    public String listPhieuNhap(Model model) {
        model.addAttribute("phieuNhaps", khoService.getAllPhieuNhap());
        return "admin/kho/phieu-nhap";
    }

    @GetMapping("/phieu-nhap/create")
    public String createPhieuNhapForm(Model model) {
        model.addAttribute("khos", khoService.getAllKho());
        model.addAttribute("nhaCungCaps", nhaCungCapRepository.findAll());
        model.addAttribute("sachs", sachRepository.findAll());
        model.addAttribute("nhanViens", nhanVienRepository.findAll());
        return "admin/kho/phieu-nhap-form";
    }

    @PostMapping("/phieu-nhap")
    @ResponseBody
    public ResponseEntity<?> createPhieuNhap(@RequestBody CreatePhieuNhapRequest request) {
        try {
            var pn = khoService.createPhieuNhap(request.getKhoId(), request.getNhaCungCapId(), request.getNhanVienId(), request.getChiTiets());
            return ResponseEntity.ok(Map.of("id", pn.getMaPN(), "success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/phieu-nhap/{id}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmPhieuNhap(@PathVariable Long id) {
        try {
            khoService.confirmPhieuNhap(id.intValue());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/phieu-ke")
    public String listPhieuKe(Model model) {
        model.addAttribute("phieuKes", khoService.getAllPhieuKe());
        return "admin/kho/phieu-ke";
    }

    @GetMapping("/phieu-ke/create")
    public String createPhieuKeForm(Model model) {
        model.addAttribute("khos", khoService.getAllKho());
        model.addAttribute("sachs", sachRepository.findAll());
        model.addAttribute("nhanViens", nhanVienRepository.findAll());
        return "admin/kho/phieu-ke-form";
    }

    @PostMapping("/phieu-ke")
    @ResponseBody
    public ResponseEntity<?> createPhieuKe(@RequestBody CreatePhieuKeRequest request) {
        try {
            var pk = khoService.createPhieuKe(request.getKhoId(), request.getNhanVienId(), request.getChiTiets(), request.getGhiChu());
            return ResponseEntity.ok(Map.of("id", pk.getMaPhieuKiemKe(), "success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/phieu-ke/{id}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmPhieuKe(@PathVariable Long id) {
        try {
            khoService.confirmPhieuKe(id.intValue());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/warnings")
    public String lowStockWarnings(@RequestParam Long khoId, Model model) {
        model.addAttribute("warnings", khoService.getLowStockWarnings(khoId.intValue()));
        model.addAttribute("kho", khoService.getKhoById(khoId.intValue()));
        return "admin/kho/warnings";
    }
}