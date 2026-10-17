package vn.bookstore.the4bookstore.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Quản lý tồn kho sàn đa người bán:
 * Vendor tự quản lý tồn kho tại gian hàng (/vendor/inventory), không dùng yêu cầu cấp hàng tập trung.
 */
@Controller
@RequestMapping("/vendor/stock-requests")
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'MANAGER')")
public class VendorStockController {

    @GetMapping({"", "/**"})
    public String redirectStockRequests(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "Mô hình sàn đa người bán: Vendor trực tiếp quản trị tồn kho tại gian hàng.");
        return "redirect:/vendor/inventory";
    }
}
