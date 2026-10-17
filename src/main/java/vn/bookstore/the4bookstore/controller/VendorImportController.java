package vn.bookstore.the4bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/vendor/imports")
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'MANAGER')")
public class VendorImportController {

    public VendorImportController() {
    }

    // --- 1. DANH SÁCH PHIẾU NHẬP HÀNG CỦA VENDOR (Đã gỡ bỏ, chuyển hướng về Dashboard) ---
    @GetMapping
    public String listImports() {
        return "redirect:/vendor/dashboard";
    }

    // --- 2. TRANG LẬP PHIẾU NHẬP HÀNG (Đã gỡ bỏ, chuyển hướng về Dashboard) ---
    @GetMapping("/create")
    public String showCreateImportForm() {
        return "redirect:/vendor/dashboard";
    }

    // --- 3. LƯU & XÁC NHẬN PHIẾU NHẬP HÀNG ---
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> submitImport() {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Chức năng nhập hàng tập trung đã được gỡ bỏ khỏi kênh người bán. Vui lòng quản lý tồn kho tại mục Kiểm Kê Kho hoặc sửa sản phẩm."
        ));
    }

    // --- 4. CHI TIẾT PHIẾU NHẬP HÀNG ---
    @GetMapping("/{id}")
    public String viewImportDetail(@PathVariable Integer id) {
        return "redirect:/vendor/dashboard";
    }
}
