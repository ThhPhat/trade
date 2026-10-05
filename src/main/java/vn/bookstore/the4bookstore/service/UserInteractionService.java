package vn.bookstore.the4bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.*;
import vn.bookstore.the4bookstore.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInteractionService {

    private final SanPhamYeuThichRepository yeuThichRepository;
    private final SanPhamDaXemRepository daXemRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final DanhGiaMediaRepository danhGiaMediaRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public UserInteractionService(SanPhamYeuThichRepository yeuThichRepository,
                                  SanPhamDaXemRepository daXemRepository,
                                  SanPhamRepository sanPhamRepository,
                                  DanhGiaRepository danhGiaRepository,
                                  DanhGiaMediaRepository danhGiaMediaRepository,
                                  DonHangRepository donHangRepository,
                                  ChiTietDonHangRepository chiTietDonHangRepository) {
        this.yeuThichRepository = yeuThichRepository;
        this.daXemRepository = daXemRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.danhGiaMediaRepository = danhGiaMediaRepository;
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
    }

    // --- YÊU THÍCH (WISHLIST) ---
    public boolean toggleFavorite(KhachHang khachHang, Integer maSP) {
        SanPham sanPham = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        SanPhamYeuThichId id = new SanPhamYeuThichId(khachHang.getMaKH(), maSP);
        if (yeuThichRepository.existsById(id)) {
            yeuThichRepository.deleteById(id);
            return false; // Đã bỏ thích
        } else {
            SanPhamYeuThich yt = new SanPhamYeuThich(id, khachHang, sanPham, LocalDateTime.now());
            yeuThichRepository.save(yt);
            return true; // Đã thích
        }
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(KhachHang khachHang, Integer maSP) {
        if (khachHang == null) return false;
        SanPhamYeuThichId id = new SanPhamYeuThichId(khachHang.getMaKH(), maSP);
        return yeuThichRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<SanPham> getFavorites(KhachHang khachHang) {
        return yeuThichRepository.findByKhachHangOrderByNgayThichDesc(khachHang)
                .stream().map(SanPhamYeuThich::getSanPham).collect(Collectors.toList());
    }

    // --- SẢN PHẨM ĐÃ XEM (RECENTLY VIEWED) ---
    public void recordView(KhachHang khachHang, Integer maSP) {
        if (khachHang == null || maSP == null) return;
        SanPham sanPham = sanPhamRepository.findById(maSP).orElse(null);
        if (sanPham == null) return;

        SanPhamDaXemId id = new SanPhamDaXemId(khachHang.getMaKH(), maSP);
        SanPhamDaXem dx = daXemRepository.findById(id).orElseGet(() -> {
            SanPhamDaXem newDx = new SanPhamDaXem();
            newDx.setId(id);
            newDx.setKhachHang(khachHang);
            newDx.setSanPham(sanPham);
            return newDx;
        });
        dx.setThoiGianXem(LocalDateTime.now());
        daXemRepository.save(dx);
    }

    @Transactional(readOnly = true)
    public List<SanPham> getRecentlyViewed(KhachHang khachHang) {
        if (khachHang == null) return List.of();
        return daXemRepository.findTop10ByKhachHangOrderByThoiGianXemDesc(khachHang)
                .stream().map(SanPhamDaXem::getSanPham).collect(Collectors.toList());
    }

    // --- ĐÁNH GIÁ SẢN PHẨM ĐÃ MUA ---
    public DanhGia submitReview(KhachHang khachHang, Integer maSP, Integer maDH, int soSao, String noiDung, List<String> mediaUrls) {
        if (noiDung == null || noiDung.trim().length() < 50) {
            throw new IllegalArgumentException("Nội dung đánh giá phải có tối thiểu 50 ký tự!");
        }

        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + maDH));

        if (!donHang.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new SecurityException("Bạn không thể đánh giá đơn hàng của người khác!");
        }

        if (!"DaGiao".equalsIgnoreCase(donHang.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể đánh giá sau khi đơn hàng đã được giao thành công!");
        }

        SanPham sanPham = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        DanhGia danhGia = new DanhGia();
        danhGia.setKhachHang(khachHang);
        danhGia.setSanPham(sanPham);
        danhGia.setDonHang(donHang);
        danhGia.setSoSao(Math.max(1, Math.min(5, soSao)));
        danhGia.setNoiDung(noiDung.trim());
        danhGia.setNgayDanhGia(LocalDateTime.now());
        danhGia.setTrangThai("HienThi");

        DanhGia saved = danhGiaRepository.save(danhGia);

        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            for (String url : mediaUrls) {
                if (url != null && !url.isBlank()) {
                    String loai = (url.endsWith(".mp4") || url.endsWith(".webm")) ? "VIDEO" : "IMAGE";
                    DanhGiaMedia media = new DanhGiaMedia();
                    media.setDanhGia(saved);
                    media.setUrl(url);
                    media.setLoaiMedia(loai);
                    danhGiaMediaRepository.save(media);
                }
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<DanhGia> getReviewsForProduct(SanPham sanPham) {
        if (sanPham == null) return List.of();
        return danhGiaRepository.findBySanPhamAndTrangThaiOrderByNgayDanhGiaDesc(sanPham, "HienThi");
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(SanPham sanPham) {
        if (sanPham == null) return 5.0;
        Double avg = danhGiaRepository.findAverageRatingBySanPham(sanPham);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0;
    }

    @Transactional(readOnly = true)
    public List<Integer> getEligibleOrderIdsForReview(KhachHang khachHang, Integer maSP) {
        if (khachHang == null || maSP == null) return List.of();
        List<Integer> deliveredOrders = chiTietDonHangRepository.findDeliveredOrderIdsByCustomerAndProduct(khachHang.getMaKH(), maSP);
        SanPham sanPham = sanPhamRepository.findById(maSP).orElse(null);
        if (sanPham == null) return List.of();

        return deliveredOrders.stream().filter(maDH -> {
            DonHang dh = donHangRepository.findById(maDH).orElse(null);
            if (dh == null) return false;
            return !danhGiaRepository.existsByKhachHangAndSanPhamAndDonHang(khachHang, sanPham, dh);
        }).collect(Collectors.toList());
    }
}
