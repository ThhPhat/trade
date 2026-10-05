package vn.bookstore.the4bookstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class VendorService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ShopRepository shopRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DonHangRepository donHangRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final DanhMucRepository danhMucRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final SanPhamService sanPhamService;

    public VendorService(ShopRepository shopRepository,
                         SanPhamRepository sanPhamRepository,
                         DonHangRepository donHangRepository,
                         KhuyenMaiRepository khuyenMaiRepository,
                         DanhMucRepository danhMucRepository,
                         NhaXuatBanRepository nhaXuatBanRepository,
                         SanPhamService sanPhamService) {
        this.shopRepository = shopRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.donHangRepository = donHangRepository;
        this.khuyenMaiRepository = khuyenMaiRepository;
        this.danhMucRepository = danhMucRepository;
        this.nhaXuatBanRepository = nhaXuatBanRepository;
        this.sanPhamService = sanPhamService;
    }

    // --- DASHBOARD THỐNG KÊ CỦA SHOP ---
    @Transactional(readOnly = true)
    public Map<String, Object> getShopDashboardStats(Integer maShop) {
        Map<String, Object> stats = new HashMap<>();

        long totalBooks = sanPhamRepository.countActiveByShop(maShop);
        long newOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DonHangMoi");
        long confirmedOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaXacNhan");
        long pickedUpOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaLayHang");
        long shippingOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DangGiao");
        long completedOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaGiao");
        long cancelledOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaHuy");
        long returnRefundOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "TraHangHoanTien");
        long disputeOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "TranhChap");

        Long totalRevenue = donHangRepository.getShopRevenue(maShop);
        Long platformFee = donHangRepository.getShopPlatformFeePaid(maShop);

        stats.put("totalBooks", totalBooks);
        stats.put("newOrders", newOrders);
        stats.put("confirmedOrders", confirmedOrders);
        stats.put("pickedUpOrders", pickedUpOrders);
        stats.put("shippingOrders", shippingOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("returnRefundOrders", returnRefundOrders);
        stats.put("disputeOrders", disputeOrders);

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0L);
        stats.put("platformFee", platformFee != null ? platformFee : 0L);
        stats.put("netPayout", (totalRevenue != null ? totalRevenue : 0L));

        return stats;
    }

    // --- QUẢN LÝ SẢN PHẨM CỦA SHOP ---
    @Transactional(readOnly = true)
    public Page<SanPham> getShopProducts(Integer maShop, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return sanPhamRepository.findActiveByShopAndKeyword(maShop, keyword.trim(), pageable);
        }
        return sanPhamRepository.findActiveByShop(maShop, pageable);
    }

    public SanPham saveShopProduct(Integer maShop, SanPham sanPham, Integer maDanhMuc, Integer maNXB) {
        Shop shop = shopRepository.findById(maShop)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shop: " + maShop));

        // Auto-generate ISBN if empty or null so user never has to invent or type code
        if (sanPham.getISBN() != null && !sanPham.getISBN().trim().isEmpty()) {
            sanPham.setISBN(sanPham.getISBN().trim());
        } else {
            sanPham.setISBN("BK-" + (System.currentTimeMillis() % 100000000));
        }
        if (sanPham.getMucTonToiThieu() == null) {
            sanPham.setMucTonToiThieu(0);
        }
        if (sanPham.getSoLuongTon() == null) {
            sanPham.setSoLuongTon(0);
        }
        if (sanPham.getGiaBan() == null) {
            sanPham.setGiaBan(0);
        }

        if (sanPham.getMaSP() != null) {
            SanPham existing = sanPhamRepository.findById(sanPham.getMaSP())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
            if (existing.getShop() == null || !existing.getShop().getMaShop().equals(maShop)) {
                throw new SecurityException("Bạn không có quyền chỉnh sửa sản phẩm của shop khác!");
            }
            existing.setTenSP(sanPham.getTenSP());
            existing.setGiaBan(sanPham.getGiaBan());
            existing.setSoLuongTon(sanPham.getSoLuongTon());
            existing.setMoTa(sanPham.getMoTa());
            existing.setISBN(sanPham.getISBN());
            existing.setLoaiSP(sanPham.getLoaiSP() != null ? sanPham.getLoaiSP() : "sach");
            if (sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isBlank()) {
                existing.setHinhAnh(sanPham.getHinhAnh());
            }
            existing.setTrangThai(sanPham.getTrangThai());

            if (maDanhMuc != null) {
                existing.setDanhMuc(danhMucRepository.findById(maDanhMuc).orElse(null));
            }
            if (maNXB != null) {
                existing.setNhaXuatBan(nhaXuatBanRepository.findById(maNXB).orElse(null));
            }
            return sanPhamRepository.save(existing);
        } else {
            sanPham.setShop(shop);
            sanPham.setTrangThaiKhoa("BinhThuong");
            sanPham.setSoLuongDaBan(0);
            sanPham.setNgayTao(LocalDateTime.now());
            if (sanPham.getLoaiSP() == null) sanPham.setLoaiSP("sach");
            if (maDanhMuc != null) {
                sanPham.setDanhMuc(danhMucRepository.findById(maDanhMuc).orElse(null));
            }
            if (sanPham.getDanhMuc() == null) {
                sanPham.setDanhMuc(danhMucRepository.findAll().stream().findFirst().orElse(null));
            }
            if (maNXB != null) {
                sanPham.setNhaXuatBan(nhaXuatBanRepository.findById(maNXB).orElse(null));
            }
            return sanPhamRepository.save(sanPham);
        }
    }

    public void deleteShopProduct(Integer maShop, Integer maSP) {
        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        if (sp.getShop() == null || !sp.getShop().getMaShop().equals(maShop)) {
            throw new SecurityException("Không có quyền xóa sản phẩm của shop khác!");
        }
        sanPhamService.deleteProduct(maSP);
    }

    // --- QUY TRÌNH QUẢN LÝ ĐƠN HÀNG 7 TRẠNG THÁI ---
    @Transactional(readOnly = true)
    public Page<DonHang> getShopOrders(Integer maShop, String trangThai, Pageable pageable) {
        if (trangThai != null && !trangThai.isBlank() && !"ALL".equalsIgnoreCase(trangThai)) {
            return donHangRepository.findByShop_MaShopAndTrangThaiOrderByNgayDatDesc(maShop, trangThai, pageable);
        }
        return donHangRepository.findByShop_MaShopOrderByNgayDatDesc(maShop, pageable);
    }

    public DonHang xacNhanDonHang(Integer maDH, Integer maShop) {
        if (entityManager != null) {
            try {
                entityManager.createNativeQuery("CALL sp_VendorXacNhanDonHang(:maDH, :maShop, @status, @message)")
                        .setParameter("maDH", maDH)
                        .setParameter("maShop", maShop)
                        .executeUpdate();
                Object statusObj = entityManager.createNativeQuery("SELECT @status").getSingleResult();
                Object messageObj = entityManager.createNativeQuery("SELECT @message").getSingleResult();
                String status = statusObj != null ? statusObj.toString() : "";
                String message = messageObj != null ? messageObj.toString() : "";
                if ("SUCCESS".equalsIgnoreCase(status)) {
                    return donHangRepository.findById(maDH).orElse(null);
                } else if ("OUT_OF_STOCK".equalsIgnoreCase(status) || "NOT_FOUND".equalsIgnoreCase(status) || "INVALID_STATE".equalsIgnoreCase(status)) {
                    throw new IllegalStateException(message);
                }
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ignored) {
                // Fallback to standard JPA flow if SP not supported or in unit tests
            }
        }
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        if (!"DonHangMoi".equalsIgnoreCase(dh.getTrangThai()) && !"ChoXuLy".equalsIgnoreCase(dh.getTrangThai()) && !"ChoXacNhan".equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xác nhận!");
        }
        dh.setTrangThai("DaXacNhan");
        dh.setNgayXacNhan(LocalDateTime.now());
        return donHangRepository.save(dh);
    }

    public DonHang daLayHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        if (!"DaXacNhan".equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng phải được xác nhận trước khi bàn giao vận chuyển!");
        }
        dh.setTrangThai("DaLayHang");
        return donHangRepository.save(dh);
    }

    public DonHang dangGiaoHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DangGiao");
        return donHangRepository.save(dh);
    }

    public DonHang daGiaoHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DaGiao");
        dh.setNgayHoanThanh(LocalDateTime.now());
        dh.setTrangThaiThanhToan("DaThanhToan");

        // Tính phí sàn và doanh thu thực nhận
        BigDecimal feeRate = dh.getShop().getChietKhauPhanTram() != null ? dh.getShop().getChietKhauPhanTram() : new BigDecimal("5.00");
        int tongTien = dh.getTongTien() != null ? dh.getTongTien() : 0;
        int feeAmount = feeRate.multiply(new BigDecimal(tongTien)).divide(new BigDecimal(100), java.math.RoundingMode.HALF_UP).intValue();

        dh.setChietKhauAppPhanTram(feeRate);
        dh.setTienPhiSan(feeAmount);
        dh.setTienThucNhanShop(tongTien - feeAmount);

        // Tăng số lượng đã bán cho sản phẩm
        if (dh.getChiTietDonHangs() != null) {
            for (ChiTietDonHang ctdh : dh.getChiTietDonHangs()) {
                SanPham sp = ctdh.getSanPham();
                if (sp != null) {
                    sp.setSoLuongDaBan(sp.getSoLuongDaBan() + ctdh.getSoLuong());
                    sanPhamRepository.save(sp);
                }
            }
        }

        return donHangRepository.save(dh);
    }

    public DonHang huyDonHang(Integer maDH, Integer maShop, String lyDo) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DaHuy");
        dh.setLyDoHuy(lyDo);
        return donHangRepository.save(dh);
    }

    public DonHang dongYTraHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("TraHangHoanTien");
        dh.setTrangThaiThanhToan("DaHoanTien");
        return donHangRepository.save(dh);
    }

    public DonHang tuChoiTraHang(Integer maDH, Integer maShop, String lyDo) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        // Chuyển sang TranhChap để Manager giải quyết
        dh.setTrangThai("TranhChap");
        dh.setLyDoTuChoi(lyDo);
        return donHangRepository.save(dh);
    }

    private DonHang checkAndGetShopOrder(Integer maDH, Integer maShop) {
        DonHang dh = donHangRepository.findById(maDH)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + maDH));
        if (dh.getShop() == null || !dh.getShop().getMaShop().equals(maShop)) {
            throw new SecurityException("Không có quyền can thiệp vào đơn hàng của shop khác!");
        }
        return dh;
    }

    // --- KHUYẾN MÃI CỦA SHOP ---
    @Transactional(readOnly = true)
    public List<KhuyenMai> getShopPromotions(Integer maShop) {
        return khuyenMaiRepository.findByShop_MaShopOrderByMaKMDesc(maShop);
    }

    public KhuyenMai createShopPromotion(Integer maShop, KhuyenMai km) {
        Shop shop = shopRepository.findById(maShop)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shop: " + maShop));
        km.setShop(shop);
        km.setPhamVi("SHOP");
        km.setTrangThai("HoatDong");
        return khuyenMaiRepository.save(km);
    }
}
