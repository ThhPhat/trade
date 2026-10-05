package vn.bookstore.the4bookstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.repository.KhuyenMaiRepository;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;
import vn.bookstore.the4bookstore.service.ShopService;

import java.util.List;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopStorefrontController {

    private final ShopService shopService;
    private final SanPhamRepository sanPhamRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;

    @GetMapping("/{slug}")
    public String shopPage(@PathVariable String slug,
                           @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Shop shop = shopService.findBySlug(slug)
                .orElseGet(() -> {
                    try {
                        return shopService.findById(Integer.parseInt(slug)).orElse(null);
                    } catch (Exception e) {
                        return null;
                    }
                });

        if (shop == null) {
            return "redirect:/?shopNotFound=true";
        }

        if (!"HoatDong".equalsIgnoreCase(shop.getTrangThai())) {
            return "redirect:/?shopPending=true";
        }

        Page<SanPham> bookPage;
        if (q != null && !q.isBlank()) {
            bookPage = sanPhamRepository.findActiveBooksByShopAndKeyword(shop.getMaShop(), q.trim(), PageRequest.of(page, 12));
        } else {
            bookPage = sanPhamRepository.findActiveBooksByShop(shop.getMaShop(), PageRequest.of(page, 12));
        }

        List<KhuyenMai> vouchers = khuyenMaiRepository.findByShop_MaShopOrderByMaKMDesc(shop.getMaShop());

        model.addAttribute("shop", shop);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("page", bookPage);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("keyword", q != null ? q : "");
        return "shop/index";
    }
}
