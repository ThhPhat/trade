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
    private final ThanhToanRepository thanhToanRepository;

    public VendorService(ShopRepository shopRepository,
                         SanPhamRepository sanPhamRepository,
                         DonHangRepository donHangRepository,
                         KhuyenMaiRepository khuyenMaiRepository,
                         DanhMucRepository danhMucRepository,
                         NhaXuatBanRepository nhaXuatBanRepository,
                         SanPhamService sanPhamService,
                         ThanhToanRepository thanhToanRepository) {
        this.shopRepository = shopRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.donHangRepository = donHangRepository;
        this.khuyenMaiRepository = khuyenMaiRepository;
        this.danhMucRepository = danhMucRepository;
        this.nhaXuatBanRepository = nhaXuatBanRepository;
        this.sanPhamService = sanPhamService;
        this.thanhToanRepository = thanhToanRepository;
    }

    // --- DASHBOARD THỐNG KÊ CỦA SHOP ---
    @Transactional(readOnly = true)
    public Map<String, Object> getShopDashboardStats(Integer maShop) {
        Map<String, Object> stats = new HashMap<>();

        long totalBooks = sanPhamRepository.countActiveByShop(maShop);
        long newOrders = donHangRepository.countByShop_MaShopAndTrangThaiIn(maShop, List.of("DonHangMoi", "ChoXuLy"));
        long confirmedOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaXacNhan");
        long pickedUpOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaLayHang");
        long shippingOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DangGiao");
        long deliveredOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "DaGiao");
        long completedOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "HoanTat");
        long cancelRequests = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "YeuCauHuy");
        long cancelledOrders = donHangRepository.countByShop_MaShopAndTrangThaiIn(maShop, List.of("DaHuy", "Huy"));
        long returnRefundOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "TraHangHoanTien");
        long disputeOrders = donHangRepository.countByShop_MaShopAndTrangThai(maShop, "TranhChap");

        Long totalRevenue = donHangRepository.getShopRevenue(maShop);
        Long platformFee = donHangRepository.getShopPlatformFeePaid(maShop);

        stats.put("totalBooks", totalBooks);
        stats.put("newOrders", newOrders);
        stats.put("confirmedOrders", confirmedOrders);
        stats.put("pickedUpOrders", pickedUpOrders);
        stats.put("shippingOrders", shippingOrders);
        stats.put("deliveredOrders", deliveredOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelRequests", cancelRequests);
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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private TacGiaRepository tacGiaRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SanPhamTacGiaRepository sanPhamTacGiaRepository;

    public void setTacGiaRepository(TacGiaRepository tacGiaRepository) {
        this.tacGiaRepository = tacGiaRepository;
    }

    public void setSanPhamTacGiaRepository(SanPhamTacGiaRepository sanPhamTacGiaRepository) {
        this.sanPhamTacGiaRepository = sanPhamTacGiaRepository;
    }

    public SanPham saveShopProduct(Integer maShop, SanPham sanPham, Integer maDanhMuc, Integer maNXB) {
        return saveShopProduct(maShop, sanPham, maDanhMuc, maNXB, null, null);
    }

    public SanPham saveShopProduct(Integer maShop, SanPham sanPham, Integer maDanhMuc, Integer maNXB, String tenNXB) {
        return saveShopProduct(maShop, sanPham, maDanhMuc, maNXB, tenNXB, null);
    }

    public SanPham saveShopProduct(Integer maShop, SanPham sanPham, Integer maDanhMuc, Integer maNXB, String tenNXB, String tenTacGia) {
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

        // Xử lý Nhà Xuất Bản do Vendor tự do nhập hoặc chọn từ gợi ý
        NhaXuatBan resolvedNXB = null;
        if (tenNXB != null && !tenNXB.trim().isEmpty()) {
            String cleanTenNXB = tenNXB.trim();
            resolvedNXB = nhaXuatBanRepository.findByTenNXBIgnoreCase(cleanTenNXB)
                    .orElseGet(() -> {
                        NhaXuatBan newNxb = new NhaXuatBan();
                        newNxb.setTenNXB(cleanTenNXB);
                        return nhaXuatBanRepository.save(newNxb);
                    });
        } else if (maNXB != null) {
            resolvedNXB = nhaXuatBanRepository.findById(maNXB).orElse(null);
        }

        SanPham saved;
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
            if (resolvedNXB != null || (tenNXB != null && tenNXB.trim().isEmpty())) {
                existing.setNhaXuatBan(resolvedNXB);
            } else if (maNXB != null) {
                existing.setNhaXuatBan(resolvedNXB);
            }
            saved = sanPhamRepository.save(existing);
        } else {
            sanPham.setShop(shop);
            sanPham.setTrangThaiKhoa("ChoDuyet");
            sanPham.setSoLuongDaBan(0);
            sanPham.setNgayTao(LocalDateTime.now());
            if (sanPham.getLoaiSP() == null) sanPham.setLoaiSP("sach");
            if (maDanhMuc != null) {
                sanPham.setDanhMuc(danhMucRepository.findById(maDanhMuc).orElse(null));
            }
            if (sanPham.getDanhMuc() == null) {
                sanPham.setDanhMuc(danhMucRepository.findAll().stream().findFirst().orElse(null));
            }
            sanPham.setNhaXuatBan(resolvedNXB);
            saved = sanPhamRepository.save(sanPham);
        }

        // Xử lý Tác Giả (hỗ trợ 1 hoặc nhiều tác giả cách nhau bởi dấu phẩy/chấm phẩy)
        if (tenTacGia != null) {
            if (sanPhamTacGiaRepository != null && saved.getMaSP() != null) {
                sanPhamTacGiaRepository.deleteBySanPhamId(saved.getMaSP());
            }
            if (!tenTacGia.trim().isEmpty() && tacGiaRepository != null && sanPhamTacGiaRepository != null) {
                String[] authorNames = tenTacGia.split("[,;]");
                int order = 1;
                List<SanPhamTacGia> sptgList = new java.util.ArrayList<>();
                for (String rawName : authorNames) {
                    String cleanName = rawName.trim();
                    if (!cleanName.isEmpty()) {
                        TacGia tg = tacGiaRepository.findByTenTacGiaIgnoreCase(cleanName)
                                .orElseGet(() -> {
                                    TacGia newTg = new TacGia();
                                    newTg.setTenTacGia(cleanName);
                                    return tacGiaRepository.save(newTg);
                                });
                        SanPhamTacGia sptg = new SanPhamTacGia(saved, tg, order++);
                        sanPhamTacGiaRepository.save(sptg);
                        sptgList.add(sptg);
                    }
                }
                saved.setSanPhamTacGias(sptgList);
            } else {
                saved.setSanPhamTacGias(new java.util.ArrayList<>());
            }
        }

        return saved;
    }

    public void deleteShopProduct(Integer maShop, Integer maSP) {
        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        if (sp.getShop() == null || !sp.getShop().getMaShop().equals(maShop)) {
            throw new SecurityException("Không có quyền xóa sản phẩm của shop khác!");
        }
        sanPhamService.deleteProduct(maSP);
    }

    // --- QUY TRÌNH QUẢN LÝ ĐƠN HÀNG 6 GIAI ĐOẠN & NGOẠI LỆ ---
    @Transactional(readOnly = true)
    public Page<DonHang> getShopOrders(Integer maShop, String trangThai, Pageable pageable) {
        if (trangThai != null && !trangThai.isBlank() && !"ALL".equalsIgnoreCase(trangThai)) {
            if ("DonHangMoi".equalsIgnoreCase(trangThai) || "ChoXuLy".equalsIgnoreCase(trangThai)) {
                return donHangRepository.findByShop_MaShopAndTrangThaiInOrderByNgayDatDesc(maShop, List.of("DonHangMoi", "ChoXuLy"), pageable);
            }
            if ("DaHuy".equalsIgnoreCase(trangThai) || "Huy".equalsIgnoreCase(trangThai)) {
                return donHangRepository.findByShop_MaShopAndTrangThaiInOrderByNgayDatDesc(maShop, List.of("DaHuy", "Huy"), pageable);
            }
            return donHangRepository.findByShop_MaShopAndTrangThaiOrderByNgayDatDesc(maShop, trangThai, pageable);
        }
        return donHangRepository.findByShop_MaShopOrderByNgayDatDesc(maShop, pageable);
    }

    // 0. Xác nhận đã thanh toán thành công
    public DonHang xacNhanThanhToan(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        if ("DaThanhToan".equalsIgnoreCase(dh.getTrangThaiThanhToan())) {
            return dh;
        }
        dh.setTrangThaiThanhToan("DaThanhToan");
        if ("ChoThanhToan".equalsIgnoreCase(dh.getTrangThai())) {
            dh.setTrangThai("ChoXuLy");
        }
        ThanhToan tt = thanhToanRepository.findFirstByDonHangOrderByMaThanhToanDesc(dh).orElse(null);
        if (tt != null) {
            tt.setTrangThai("ThanhCong");
            tt.setNgayThanhToan(LocalDateTime.now());
            thanhToanRepository.save(tt);
        } else {
            ThanhToan newTt = new ThanhToan();
            newTt.setDonHang(dh);
            newTt.setPhuongThuc(dh.getPhuongThucThanhToan() != null ? dh.getPhuongThucThanhToan() : "COD");
            newTt.setSoTien(dh.getTongTien() != null ? dh.getTongTien() : 0);
            newTt.setTrangThai("ThanhCong");
            newTt.setNgayThanhToan(LocalDateTime.now());
            newTt.setNoiDung("Shop xác nhận đã thanh toán thành công");
            thanhToanRepository.save(newTt);
        }
        return donHangRepository.save(dh);
    }

    // 1. Xác nhận đơn: Tự động sinh mã vận đơn, chuyển DaXacNhan
    public DonHang xacNhanDonHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        if (!"DonHangMoi".equalsIgnoreCase(dh.getTrangThai()) && !"ChoXuLy".equalsIgnoreCase(dh.getTrangThai()) && !"ChoXacNhan".equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xác nhận!");
        }
        dh.setTrangThai("DaXacNhan");
        dh.setNgayXacNhan(LocalDateTime.now());

        // Tự động sinh mã vận đơn nếu chưa có
        if (dh.getMaVanDon() == null || dh.getMaVanDon().isBlank()) {
            String prefix = "GHN";
            if (dh.getNhaVanChuyen() != null && dh.getNhaVanChuyen().getTenNvc() != null) {
                String ten = dh.getNhaVanChuyen().getTenNvc().toUpperCase();
                if (ten.contains("GHN") || ten.contains("GIAO HÀNG NHANH")) prefix = "GHN";
                else if (ten.contains("GHTK") || ten.contains("TIẾT KIỆM")) prefix = "GHTK";
                else if (ten.contains("VIETTEL")) prefix = "VTP";
                else if (ten.contains("VNPOST")) prefix = "VNPOST";
                else if (dh.getNhaVanChuyen().getMaNvc() != null) prefix = "NVC" + dh.getNhaVanChuyen().getMaNvc();
            }
            dh.setMaVanDon(prefix + "-" + dh.getMaDH() + "-" + (System.currentTimeMillis() % 100000));
        }

        return donHangRepository.save(dh);
    }

    // 2. Đã lấy hàng / Đang đóng gói: Bàn giao shipper
    public DonHang daLayHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        if (!"DaXacNhan".equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng phải được xác nhận trước khi bàn giao vận chuyển!");
        }
        dh.setTrangThai("DaLayHang");
        return donHangRepository.save(dh);
    }

    // 3. Đang giao hàng: Shipper đang trên đường giao
    public DonHang dangGiaoHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DangGiao");
        return donHangRepository.save(dh);
    }

    // 4. Đã giao hàng: Shipper giao thành công, kích hoạt thời gian chờ khiếu nại (tiền trong Escrow)
    public DonHang daGiaoHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DaGiao");
        dh.setTrangThaiThanhToan("DaThanhToan");
        return donHangRepository.save(dh);
    }

    // 5. Hoàn tất & Quyết toán ví shop: Tiền về ví Shop = Giá trị đơn - Phí sàn (% Commission)
    public DonHang hoanTatDonHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("HoanTat");
        dh.setNgayHoanThanh(LocalDateTime.now());
        dh.setTrangThaiThanhToan("DaThanhToan");

        // Tính phí sàn và tiền thực nhận ví shop
        BigDecimal feeRate = (dh.getShop() != null && dh.getShop().getChietKhauPhanTram() != null)
                ? dh.getShop().getChietKhauPhanTram()
                : new BigDecimal("5.00");
        int tongTien = dh.getTongTien() != null ? dh.getTongTien() : 0;
        int feeAmount = feeRate.multiply(new BigDecimal(tongTien)).divide(new BigDecimal(100), java.math.RoundingMode.HALF_UP).intValue();

        dh.setChietKhauAppPhanTram(feeRate);
        dh.setTienPhiSan(feeAmount);
        dh.setTienThucNhanShop(Math.max(0, tongTien - feeAmount));

        return donHangRepository.save(dh);
    }

    // Ngoại lệ A1: Shop chủ động hủy (do hết hàng đột xuất) -> Nhả tồn kho
    public DonHang huyDonHang(Integer maDH, Integer maShop, String lyDo) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DaHuy");
        dh.setLyDoHuy(lyDo != null && !lyDo.isBlank() ? lyDo : "Shop hết hàng đột xuất");

        // Hoàn lại tồn kho
        releaseOrderStock(dh);

        if ("DaThanhToan".equalsIgnoreCase(dh.getTrangThaiThanhToan())) {
            dh.setTrangThaiThanhToan("DaHoanTien");
        }

        return donHangRepository.save(dh);
    }

    // Ngoại lệ A2: Shop chấp nhận yêu cầu hủy từ khách (khi đơn đã xác nhận) -> Nhả tồn kho
    public DonHang chapNhanHuyDon(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DaHuy");
        dh.setLyDoHuy((dh.getLyDoHuy() != null ? dh.getLyDoHuy() + " (" : "") + "Shop chấp nhận hủy" + (dh.getLyDoHuy() != null ? ")" : ""));

        // Hoàn lại tồn kho
        releaseOrderStock(dh);

        if ("DaThanhToan".equalsIgnoreCase(dh.getTrangThaiThanhToan())) {
            dh.setTrangThaiThanhToan("DaHoanTien");
        }

        return donHangRepository.save(dh);
    }

    // Ngoại lệ A3: Shop từ chối yêu cầu hủy của khách -> Giữ nguyên DaXacNhan để tiếp tục giao
    public DonHang tuChoiHuyDon(Integer maDH, Integer maShop, String lyDo) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("DaXacNhan");
        dh.setLyDoTuChoi(lyDo != null && !lyDo.isBlank() ? lyDo : "Shop đã đóng gói và chuẩn bị bàn giao cho bưu tá");
        return donHangRepository.save(dh);
    }

    // Ngoại lệ B1: Shop đồng ý yêu cầu trả hàng / hoàn tiền -> Nhận lại sách, nhả tồn kho, hoàn tiền
    public DonHang dongYTraHang(Integer maDH, Integer maShop) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        if ("ChoXuLy".equalsIgnoreCase(dh.getTrangThai()) || "DonHangMoi".equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng chưa được xác nhận, không thể hoàn hàng!");
        }

        dh.setTrangThai("DaHoan");
        if ("DaThanhToan".equalsIgnoreCase(dh.getTrangThaiThanhToan()) || "HoanTat".equalsIgnoreCase(dh.getTrangThai())) {
            dh.setTrangThaiThanhToan("DaHoanTien");
            dh.setTienThucNhanShop(0);
            dh.setTienPhiSan(0);
        }

        // Nhận lại sách và nhả tồn kho
        releaseOrderStock(dh);

        return donHangRepository.save(dh);
    }

    // Ngoại lệ B2: Shop từ chối trả hàng -> Chuyển sang Tranh Chấp để Admin/Manager phân xử
    public DonHang tuChoiTraHang(Integer maDH, Integer maShop, String lyDo) {
        DonHang dh = checkAndGetShopOrder(maDH, maShop);
        dh.setTrangThai("TranhChap");
        dh.setLyDoTuChoi(lyDo != null && !lyDo.isBlank() ? lyDo : "Shop từ chối trả hàng");
        return donHangRepository.save(dh);
    }

    // Helper: Giải phóng và hoàn trả số lượng tồn kho của đơn hàng
    private void releaseOrderStock(DonHang dh) {
        if (dh.getChiTietDonHangs() != null) {
            for (ChiTietDonHang ct : dh.getChiTietDonHangs()) {
                if (ct.getSanPham() != null && ct.getSoLuong() != null) {
                    sanPhamRepository.increaseStock(ct.getSanPham().getMaSP(), ct.getSoLuong());
                    SanPham sp = sanPhamRepository.findById(ct.getSanPham().getMaSP()).orElse(null);
                    if (sp != null && sp.getSoLuongDaBan() != null) {
                        sp.setSoLuongDaBan(Math.max(0, sp.getSoLuongDaBan() - ct.getSoLuong()));
                        sanPhamRepository.save(sp);
                    }
                }
            }
        }
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
