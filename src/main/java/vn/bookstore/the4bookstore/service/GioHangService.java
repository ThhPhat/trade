package vn.bookstore.the4bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {

    public static final String SESSION_CART_KEY = "SESSION_CART";

    private final GioHangRepository gioHangRepository;
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final SanPhamRepository sanPhamRepository;

    public GioHangService(GioHangRepository gioHangRepository,
                          ChiTietGioHangRepository chiTietGioHangRepository,
                          SanPhamRepository sanPhamRepository) {
        this.gioHangRepository = gioHangRepository;
        this.chiTietGioHangRepository = chiTietGioHangRepository;
        this.sanPhamRepository = sanPhamRepository;
    }

    /**
     * Lấy hoặc tạo giỏ hàng cho khách hàng
     */
    @Transactional
    public GioHang getOrCreateCart(KhachHang khachHang) {
        return gioHangRepository.findByKhachHang(khachHang)
                .orElseGet(() -> {
                    GioHang gh = new GioHang();
                    gh.setKhachHang(khachHang);
                    gh.setNgayTao(LocalDateTime.now());
                    gh.setNgayCapNhat(LocalDateTime.now());
                    return gioHangRepository.save(gh);
                });
    }

    /**
     * Thêm sản phẩm vào giỏ hàng. Nếu đã có thì tăng số lượng.
     */
    @Transactional
    public void addToCart(KhachHang khachHang, Integer maSP, Integer soLuong) {
        if (soLuong == null || soLuong <= 0) soLuong = 1;

        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maSP));

        GioHang gioHang = getOrCreateCart(khachHang);
        Optional<ChiTietGioHang> existingItem = chiTietGioHangRepository
                .findByGioHangAndSanPham(gioHang, sp);

        if (existingItem.isPresent()) {
            ChiTietGioHang ct = existingItem.get();
            int newQty = ct.getSoLuong() + soLuong;
            if (newQty > sp.getSoLuongTon()) {
                newQty = sp.getSoLuongTon();
            }
            ct.setSoLuong(newQty);
            chiTietGioHangRepository.save(ct);
        } else {
            int qty = Math.min(soLuong, sp.getSoLuongTon());
            if (qty <= 0) {
                throw new RuntimeException("Sản phẩm đã hết hàng: " + sp.getTenSP());
            }
            ChiTietGioHang ct = new ChiTietGioHang();
            ct.setGioHang(gioHang);
            ct.setSanPham(sp);
            ct.setSoLuong(qty);
            chiTietGioHangRepository.save(ct);
        }

        gioHang.setNgayCapNhat(LocalDateTime.now());
        gioHangRepository.save(gioHang);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng.
     * Nếu soLuong <= 0, xóa sản phẩm khỏi giỏ.
     */
    @Transactional
    public void updateCartItemQuantity(KhachHang khachHang, Integer maSP, Integer soLuong) {
        if (soLuong != null && soLuong <= 0) {
            removeFromCart(khachHang, maSP);
            return;
        }

        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maSP));

        GioHang gioHang = getOrCreateCart(khachHang);
        Optional<ChiTietGioHang> existingItem = chiTietGioHangRepository
                .findByGioHangAndSanPham(gioHang, sp);

        if (existingItem.isPresent()) {
            ChiTietGioHang ct = existingItem.get();
            int newQty = Math.min(soLuong, sp.getSoLuongTon());
            if (newQty <= 0) {
                chiTietGioHangRepository.delete(ct);
            } else {
                ct.setSoLuong(newQty);
                chiTietGioHangRepository.save(ct);
            }
            gioHang.setNgayCapNhat(LocalDateTime.now());
            gioHangRepository.save(gioHang);
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Transactional
    public void removeFromCart(KhachHang khachHang, Integer maSP) {
        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maSP));

        GioHang gioHang = getOrCreateCart(khachHang);
        Optional<ChiTietGioHang> existingItem = chiTietGioHangRepository
                .findByGioHangAndSanPham(gioHang, sp);

        existingItem.ifPresent(chiTietGioHangRepository::delete);
        gioHang.setNgayCapNhat(LocalDateTime.now());
        gioHangRepository.save(gioHang);
    }

    /**
     * Xóa tất cả sản phẩm trong giỏ hàng
     */
    @Transactional
    public void clearCart(KhachHang khachHang) {
        Optional<GioHang> gioHangOpt = gioHangRepository.findByKhachHang(khachHang);
        if (gioHangOpt.isPresent()) {
            GioHang gioHang = gioHangOpt.get();
            chiTietGioHangRepository.deleteByGioHang(gioHang);
            gioHang.setNgayCapNhat(LocalDateTime.now());
            gioHangRepository.save(gioHang);
        }
    }

    /**
     * Lấy danh sách chi tiết giỏ hàng
     */
    public List<ChiTietGioHang> getCartItems(KhachHang khachHang) {
        Optional<GioHang> gioHangOpt = gioHangRepository.findByKhachHang(khachHang);
        if (gioHangOpt.isPresent()) {
            return chiTietGioHangRepository.findByGioHang(gioHangOpt.get());
        }
        return Collections.emptyList();
    }

    /**
     * Đếm tổng số mặt hàng trong giỏ
     */
    public int getCartItemCount(KhachHang khachHang) {
        Optional<GioHang> gioHangOpt = gioHangRepository.findByKhachHang(khachHang);
        if (gioHangOpt.isPresent()) {
            List<ChiTietGioHang> items = chiTietGioHangRepository.findByGioHang(gioHangOpt.get());
            return items.stream().mapToInt(ChiTietGioHang::getSoLuong).sum();
        }
        return 0;
    }

    /**
     * Tính tạm tính giỏ hàng
     */
    public int calculateSubtotal(KhachHang khachHang) {
        List<ChiTietGioHang> items = getCartItems(khachHang);
        return items.stream()
                .mapToInt(ct -> ct.getSanPham().getGiaBan() * ct.getSoLuong())
                .sum();
    }

    // ==================== Quản lý Giỏ hàng qua Session (cho khách chưa đăng nhập) ====================

    @SuppressWarnings("unchecked")
    public List<ChiTietGioHang> getSessionCartItems(HttpSession session) {
        if (session == null) return new ArrayList<>();
        List<ChiTietGioHang> items = (List<ChiTietGioHang>) session.getAttribute(SESSION_CART_KEY);
        if (items == null) {
            items = new ArrayList<>();
            session.setAttribute(SESSION_CART_KEY, items);
        }
        return items;
    }

    public synchronized void addToSessionCart(HttpSession session, Integer maSP, Integer soLuong) {
        if (session == null || maSP == null) return;
        if (soLuong == null || soLuong <= 0) soLuong = 1;

        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maSP));

        List<ChiTietGioHang> items = getSessionCartItems(session);
        Optional<ChiTietGioHang> existing = items.stream()
                .filter(i -> i.getSanPham() != null && i.getSanPham().getMaSP().equals(maSP))
                .findFirst();

        int maxStock = (sp.getSoLuongTon() != null) ? sp.getSoLuongTon() : 9999;
        if (maxStock <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng: " + sp.getTenSP());
        }

        if (existing.isPresent()) {
            ChiTietGioHang ct = existing.get();
            int newQty = ct.getSoLuong() + soLuong;
            if (newQty > maxStock) newQty = maxStock;
            ct.setSoLuong(newQty);
        } else {
            int qty = Math.min(soLuong, maxStock);
            ChiTietGioHang ct = new ChiTietGioHang();
            ct.setSanPham(sp);
            ct.setSoLuong(qty);
            items.add(ct);
        }
        session.setAttribute(SESSION_CART_KEY, items);
    }

    public synchronized void updateSessionCartQuantity(HttpSession session, Integer maSP, Integer soLuong) {
        if (session == null || maSP == null) return;
        if (soLuong != null && soLuong <= 0) {
            removeFromSessionCart(session, maSP);
            return;
        }

        List<ChiTietGioHang> items = getSessionCartItems(session);
        Optional<ChiTietGioHang> existing = items.stream()
                .filter(i -> i.getSanPham() != null && i.getSanPham().getMaSP().equals(maSP))
                .findFirst();

        if (existing.isPresent()) {
            ChiTietGioHang ct = existing.get();
            int maxStock = (ct.getSanPham().getSoLuongTon() != null) ? ct.getSanPham().getSoLuongTon() : 9999;
            ct.setSoLuong(Math.min(soLuong, maxStock));
        }
        session.setAttribute(SESSION_CART_KEY, items);
    }

    public synchronized void removeFromSessionCart(HttpSession session, Integer maSP) {
        if (session == null || maSP == null) return;
        List<ChiTietGioHang> items = getSessionCartItems(session);
        items.removeIf(i -> i.getSanPham() != null && i.getSanPham().getMaSP().equals(maSP));
        session.setAttribute(SESSION_CART_KEY, items);
    }

    public synchronized void clearSessionCart(HttpSession session) {
        if (session == null) return;
        session.removeAttribute(SESSION_CART_KEY);
    }

    public int getSessionCartItemCount(HttpSession session) {
        if (session == null) return 0;
        List<ChiTietGioHang> items = getSessionCartItems(session);
        return items.stream().mapToInt(ChiTietGioHang::getSoLuong).sum();
    }

    public int calculateSessionSubtotal(HttpSession session) {
        if (session == null) return 0;
        List<ChiTietGioHang> items = getSessionCartItems(session);
        return items.stream()
                .filter(i -> i.getSanPham() != null && i.getSanPham().getGiaBan() != null)
                .mapToInt(i -> i.getSanPham().getGiaBan() * i.getSoLuong())
                .sum();
    }

    /**
     * Gộp giỏ hàng session vào Database khi người dùng đăng nhập
     */
    @Transactional
    public void mergeSessionCartToDb(KhachHang khachHang, HttpSession session) {
        if (session == null || khachHang == null) return;
        List<ChiTietGioHang> sessionItems = (List<ChiTietGioHang>) session.getAttribute(SESSION_CART_KEY);
        if (sessionItems != null && !sessionItems.isEmpty()) {
            for (ChiTietGioHang item : sessionItems) {
                if (item.getSanPham() != null && item.getSanPham().getMaSP() != null) {
                    addToCart(khachHang, item.getSanPham().getMaSP(), item.getSoLuong());
                }
            }
            session.removeAttribute(SESSION_CART_KEY);
        }
    }
}
