package vn.bookstore.the4bookstore.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.dto.OrderNotificationDTO;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final GioHangService gioHangService;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final VoucherDaLuuRepository voucherDaLuuRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DonHangService(DonHangRepository donHangRepository,
                          ChiTietDonHangRepository chiTietDonHangRepository,
                          SanPhamRepository sanPhamRepository,
                          ThanhToanRepository thanhToanRepository,
                          GioHangService gioHangService,
                          KhuyenMaiRepository khuyenMaiRepository,
                          VoucherDaLuuRepository voucherDaLuuRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.gioHangService = gioHangService;
        this.khuyenMaiRepository = khuyenMaiRepository;
        this.voucherDaLuuRepository = voucherDaLuuRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Tạo đơn hàng từ giỏ hàng của khách hàng (có thể chọn lọc danh sách sản phẩm cần mua và áp mã giảm giá).
     */
    @Transactional
    public DonHang createOrder(KhachHang khachHang, String diaChiGiao,
                               String soDienThoaiGiao, String phuongThuc, String ghiChu) {
        return createOrder(khachHang, diaChiGiao, soDienThoaiGiao, phuongThuc, ghiChu, null, null);
    }

    @Transactional
    public DonHang createOrder(KhachHang khachHang, String diaChiGiao,
                               String soDienThoaiGiao, String phuongThuc, String ghiChu,
                               List<Integer> selectedProductIds) {
        return createOrder(khachHang, diaChiGiao, soDienThoaiGiao, phuongThuc, ghiChu, selectedProductIds, null);
    }

    @Transactional
    public DonHang createOrder(KhachHang khachHang, String diaChiGiao,
                               String soDienThoaiGiao, String phuongThuc, String ghiChu,
                               List<Integer> selectedProductIds,
                               String maVoucher) {

        // 1. Lấy giỏ hàng
        List<ChiTietGioHang> allCartItems = gioHangService.getCartItems(khachHang);
        if (allCartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng!");
        }

        // Lọc theo các sản phẩm được tích chọn (nếu có chỉ định)
        List<ChiTietGioHang> cartItems;
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            cartItems = allCartItems.stream()
                    .filter(ct -> selectedProductIds.contains(ct.getSanPham().getMaSP()))
                    .toList();
        } else {
            cartItems = allCartItems;
        }

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một sản phẩm để đặt hàng!");
        }

        // 2. Khóa và kiểm tra tồn kho bằng Pessimistic Lock (tránh race condition)
        List<SanPham> lockedProducts = new ArrayList<>();
        for (ChiTietGioHang ct : cartItems) {
            Integer maSP = ct.getSanPham().getMaSP();
            SanPham sp = sanPhamRepository.findByIdWithLock(maSP)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm mã: " + maSP));

            if (sp.getSoLuongTon() < ct.getSoLuong()) {
                throw new RuntimeException("Sản phẩm \"" + sp.getTenSP() + "\" không đủ số lượng tồn kho (chỉ còn " 
                        + sp.getSoLuongTon() + " cuốn)!");
            }
            lockedProducts.add(sp);
        }

        // 3. Tạo đơn hàng
        DonHang donHang = new DonHang();
        donHang.setKhachHang(khachHang);
        donHang.setNgayDat(LocalDateTime.now());
        donHang.setDiaChiGiao(diaChiGiao);
        donHang.setSoDienThoaiGiao(soDienThoaiGiao);
        donHang.setTrangThai("ChoXuLy");
        donHang.setTienGiam(0);

        // Lưu đơn hàng trước để lấy maDH
        donHang = donHangRepository.save(donHang);

        // 4. Tạo chi tiết đơn hàng, trừ tồn kho an toàn và tính tổng tiền
        int tongTien = 0;
        List<ChiTietDonHang> chiTietList = new ArrayList<>();

        for (int i = 0; i < cartItems.size(); i++) {
            ChiTietGioHang ctGH = cartItems.get(i);
            SanPham sp = lockedProducts.get(i);

            ChiTietDonHang ctDH = new ChiTietDonHang();
            ctDH.setDonHang(donHang);
            ctDH.setSanPham(sp);
            ctDH.setSoLuong(ctGH.getSoLuong());
            ctDH.setDonGia(sp.getGiaBan());

            chiTietDonHangRepository.save(ctDH);
            chiTietList.add(ctDH);

            // 5. Trừ tồn kho
            sp.setSoLuongTon(sp.getSoLuongTon() - ctGH.getSoLuong());
            sanPhamRepository.save(sp);

            tongTien += sp.getGiaBan() * ctGH.getSoLuong();
        }

        // 6. Xử lý giảm giá từ mã khuyến mãi (chỉ chấp nhận mã từ trang Admin Khuyến Mãi)
        int tienGiam = 0;
        KhuyenMai khuyenMai = null;
        if (maVoucher != null && !maVoucher.isBlank()) {
            String cleanCode = maVoucher.trim().toUpperCase();
            Optional<KhuyenMai> kmOpt = khuyenMaiRepository.findByMaCode(cleanCode);
            if (kmOpt.isEmpty()) {
                throw new RuntimeException("Mã giảm giá [" + cleanCode + "] không tồn tại trên hệ thống!");
            }

            KhuyenMai km = kmOpt.get();
            if (!"HoatDong".equalsIgnoreCase(km.getTrangThai())) {
                throw new RuntimeException("Mã giảm giá [" + cleanCode + "] đã hết hạn hoặc tạm ngưng sử dụng!");
            }
            if (km.getNgayBatDau() != null && LocalDateTime.now().isBefore(km.getNgayBatDau())) {
                throw new RuntimeException("Mã giảm giá [" + cleanCode + "] chưa đến ngày áp dụng!");
            }
            if (km.getNgayKetThuc() != null && LocalDateTime.now().isAfter(km.getNgayKetThuc())) {
                throw new RuntimeException("Mã giảm giá [" + cleanCode + "] đã hết hạn sử dụng!");
            }
            if (km.getSoLuongToiDa() != null && km.getSoLuongDaDung() != null && km.getSoLuongDaDung() >= km.getSoLuongToiDa()) {
                throw new RuntimeException("Mã giảm giá [" + cleanCode + "] đã hết lượt sử dụng!");
            }
            if (km.getDonToiThieu() != null && tongTien < km.getDonToiThieu()) {
                throw new RuntimeException("Đơn hàng chưa đạt mức tối thiểu " + String.format("%,d đ", km.getDonToiThieu()) + " để áp dụng mã giảm giá này!");
            }

            if ("PhanTram".equalsIgnoreCase(km.getLoaiGiam())) {
                tienGiam = (int) Math.round(tongTien * (km.getGiaTriGiam() / 100.0));
                if (km.getGiamToiDa() != null && tienGiam > km.getGiamToiDa()) {
                    tienGiam = km.getGiamToiDa();
                }
            } else if ("TienCoDinh".equalsIgnoreCase(km.getLoaiGiam())) {
                tienGiam = Math.min(tongTien, km.getGiaTriGiam());
            } else if ("Freeship".equalsIgnoreCase(km.getLoaiGiam())) {
                tienGiam = km.getGiaTriGiam() != null ? km.getGiaTriGiam() : 30000;
            } else {
                tienGiam = Math.min(tongTien, km.getGiaTriGiam() != null ? km.getGiaTriGiam() : 0);
            }

            km.setSoLuongDaDung(km.getSoLuongDaDung() != null ? km.getSoLuongDaDung() + 1 : 1);
            khuyenMaiRepository.save(km);
            khuyenMai = km;

            // Nếu khách hàng đã lưu voucher này trong ví, đánh dấu là đã dùng
            try {
                if (khachHang != null) {
                    Optional<VoucherDaLuu> vdlOpt = voucherDaLuuRepository.findByKhachHangAndKhuyenMai(khachHang, km);
                    if (vdlOpt.isPresent()) {
                        VoucherDaLuu vdl = vdlOpt.get();
                        vdl.setTrangThai("DaDung");
                        vdl.setNgaySuDung(LocalDateTime.now());
                        voucherDaLuuRepository.save(vdl);
                    }
                }
            } catch (Exception ignored) {}
        }

        int finalTotal = Math.max(0, tongTien - tienGiam);
        donHang.setTongTien(finalTotal);
        donHang.setTienGiam(tienGiam);
        donHang.setKhuyenMai(khuyenMai);
        donHang.setChiTietDonHangs(chiTietList);
        donHang = donHangRepository.save(donHang);

        // 7. Tạo bản ghi thanh toán
        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setDonHang(donHang);
        thanhToan.setPhuongThuc(phuongThuc != null ? phuongThuc : "COD");
        thanhToan.setTrangThai("ChoThanhToan");
        thanhToan.setSoTien(finalTotal);
        thanhToan.setNoiDung("Thanh toán đơn hàng #" + donHang.getMaDH());
        thanhToanRepository.save(thanhToan);

        // 8. Xóa các món đã đặt khỏi giỏ hàng
        for (ChiTietGioHang ctGH : cartItems) {
            gioHangService.removeFromCart(khachHang, ctGH.getSanPham().getMaSP());
        }

        // 9. Bắn thông báo Real-time qua WebSocket cho Admin Dashboard
        try {
            OrderNotificationDTO noti = OrderNotificationDTO.builder()
                    .maDH(donHang.getMaDH())
                    .tenKhachHang(khachHang.getHoTen() != null ? khachHang.getHoTen() : "Khách hàng #" + khachHang.getMaKH())
                    .soDienThoai(donHang.getSoDienThoaiGiao())
                    .diaChiGiao(donHang.getDiaChiGiao())
                    .tongTien(donHang.getTongTien())
                    .soLuongMon(cartItems.size())
                    .trangThai(donHang.getTrangThai())
                    .ngayDat(donHang.getNgayDat())
                    .message("Có đơn hàng mới #" + donHang.getMaDH() + " từ " + (khachHang.getHoTen() != null ? khachHang.getHoTen() : "Khách hàng"))
                    .build();
            messagingTemplate.convertAndSend("/topic/admin/orders", noti);
        } catch (Exception ignored) {
            // Không làm gián đoạn transaction đặt hàng nếu WebSocket gặp sự cố
        }

        return donHang;
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng, sắp xếp theo ngày đặt giảm dần
     */
    public List<DonHang> getOrdersByKhachHang(KhachHang khachHang) {
        return donHangRepository.findByKhachHangOrderByNgayDatDesc(khachHang);
    }

    /**
     * Lấy đơn hàng theo ID
     */
    public DonHang getOrderById(Integer maDH) {
        return donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));
    }

    /**
     * Hủy đơn hàng (chỉ khi trạng thái là ChoXuLy).
     * Hoàn lại số lượng tồn kho.
     */
    @Transactional
    public void cancelOrder(Integer maDH, KhachHang khachHang, String lyDoHuy) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));

        // Kiểm tra quyền sở hữu
        if (!donHang.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này!");
        }

        // Chỉ cho phép hủy khi trạng thái là ChoXuLy
        if (!"ChoXuLy".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ xử lý'!");
        }

        // Hoàn lại tồn kho
        if (donHang.getChiTietDonHangs() != null) {
            for (ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
                if (ct.getSanPham() != null && ct.getSoLuong() != null) {
                    sanPhamRepository.increaseStock(ct.getSanPham().getMaSP(), ct.getSoLuong());
                }
            }
        }

        donHang.setTrangThai("DaHuy");
        donHang.setLyDoHuy(lyDoHuy != null && !lyDoHuy.isBlank() ? lyDoHuy : "Khách hàng tự hủy");
        donHangRepository.save(donHang);
    }
}
