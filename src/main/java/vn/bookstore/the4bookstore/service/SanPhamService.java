package vn.bookstore.the4bookstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SanPhamService {

    public static final List<String> CUSTOMER_STATUSES = List.of("DangBan", "HetHang");

    private final SanPhamRepository sanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final SanPhamTacGiaRepository sanPhamTacGiaRepository;
    private final SanPhamYeuThichRepository sanPhamYeuThichRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Thuật toán tuyển chọn "SÁCH NỔI BẬT THÁNG" (Book of the Month Spotlight Algorithm):
     *
     * 1. Tập ứng viên (Candidates):
     *    Lấy danh sách các cuốn sách đang bán, không bị khóa, shop hoạt động, còn tồn kho.
     *
     * 2. Công thức chấm điểm đa tiêu chí (Multi-factor Weighted Scoring Formula):
     *    Score = (W_recentSales * sales30d) 
     *          + (W_totalSales * totalSold) 
     *          + (W_rating * ratingScore) 
     *          + (W_favorites * favoriteCount) 
     *          + recencyBonus
     *
     *    - sales30d: Số lượng bán trong 30 ngày qua (trọng số cao nhất: x25 điểm/cuốn).
     *    - totalSold: Tổng lượng bán lũy kế (x2 điểm/cuốn).
     *    - ratingScore: Điểm đánh giá trung bình nhân logarit số lượt đánh giá.
     *    - favoriteCount: Lượt đưa vào danh sách yêu thích (x3 điểm/lượt).
     *    - recencyBonus: Điểm thưởng thời gian xuất bản gần đây (10-25 điểm).
     *
     * 3. Cơ chế Fallback an toàn:
     *    Nếu chưa có đơn hàng hoặc dữ liệu rỗng, tự động chọn cuốn sách bán chạy nhất
     *    hoặc cuốn sách mới nhất có đầy đủ hình ảnh bìa.
     */
    @Cacheable("book_of_the_month")
    public SanPham getBookOfTheMonth() {
        List<SanPham> candidates = sanPhamRepository.findSpotlightCandidates(PageRequest.of(0, 15));
        if (candidates.isEmpty()) {
            Pageable p = PageRequest.of(0, 1);
            List<SanPham> fallbackList = sanPhamRepository.findTopBooksByTrangThais(CUSTOMER_STATUSES, p);
            if (fallbackList.isEmpty()) return null;
            SanPham fb = fallbackList.get(0);
            initializeSanPhamRelations(fb);
            return fb;
        }

        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        java.time.LocalDateTime ninetyDaysAgo = java.time.LocalDateTime.now().minusDays(90);

        SanPham bestBook = null;
        double maxScore = -1.0;

        for (SanPham book : candidates) {
            long recentSales = 0;
            try {
                Long rs = chiTietDonHangRepository.countSoldQuantitySince(book.getMaSP(), thirtyDaysAgo);
                if (rs != null) recentSales = rs;
            } catch (Exception ignored) {}

            long totalSold = book.getSoLuongDaBan() != null ? book.getSoLuongDaBan() : 0;

            Double avgRating = 5.0;
            long reviewCount = 0;
            try {
                Double r = danhGiaRepository.findAverageRatingBySanPham(book);
                if (r != null) avgRating = r;
                reviewCount = danhGiaRepository.countBySanPhamAndTrangThai(book, "HienThi");
            } catch (Exception ignored) {}

            long favorites = 0;
            try {
                if (sanPhamYeuThichRepository != null) {
                    favorites = sanPhamYeuThichRepository.countBySanPham(book);
                }
            } catch (Exception ignored) {}

            double recencyBonus = 0.0;
            if (book.getNgayTao() != null) {
                if (book.getNgayTao().isAfter(thirtyDaysAgo)) {
                    recencyBonus = 25.0;
                } else if (book.getNgayTao().isAfter(ninetyDaysAgo)) {
                    recencyBonus = 10.0;
                }
            }

            // Điểm đánh giá: Điểm sao kết hợp độ tin cậy log(reviewCount)
            double ratingScore = avgRating * Math.log(1.0 + reviewCount + 1.0);

            // Công thức trọng số đa tiêu chí
            double score = (recentSales * 25.0)
                         + (totalSold * 2.0)
                         + (ratingScore * 4.0)
                         + (favorites * 3.0)
                         + recencyBonus;

            // Ưu tiên sách có hình ảnh thực tế
            if (book.getHinhAnh() != null && !book.getHinhAnh().isBlank()) {
                score += 5.0;
            }

            if (score > maxScore) {
                maxScore = score;
                bestBook = book;
            }
        }

        SanPham result = bestBook != null ? bestBook : candidates.get(0);
        initializeSanPhamRelations(result);
        return result;
    }

    @Cacheable("home_featured_books")
    public List<SanPham> getFeaturedBooks() {
        List<DanhMuc> bookCategories = getCategoriesByLoaiSP("Sach");
        List<Integer> bookCatIds = bookCategories.stream().map(DanhMuc::getMaDanhMuc).toList();

        Pageable pageable = PageRequest.of(0, 8);
        List<SanPham> books;
        if (!bookCatIds.isEmpty()) {
            books = sanPhamRepository.findTopBooksByCategoriesAndTrangThais(CUSTOMER_STATUSES, bookCatIds, pageable);
            if (!books.isEmpty()) {
                initializeSanPhamRelations(books);
                return books;
            }
        }
        books = sanPhamRepository.findTopBooksByTrangThais(CUSTOMER_STATUSES, pageable);
        initializeSanPhamRelations(books);
        return books;
    }

    @Cacheable(value = "home_preview", key = "#loaiSP")
    public List<SanPham> getPreviewByLoaiSP(String loaiSP) {
        List<SanPham> list = sanPhamRepository.findTop4ByLoaiSPAndTrangThaiIn(loaiSP, CUSTOMER_STATUSES);
        initializeSanPhamRelations(list);
        return list;
    }

    @Cacheable("guest_top_sold")
    public List<SanPham> getGuestTopSold() {
        List<SanPham> list = sanPhamRepository.findTopProductsForGuest();
        initializeSanPhamRelations(list);
        return list;
    }

    public void initializeSanPhamRelations(List<SanPham> list) {
        if (list == null || list.isEmpty()) return;
        for (SanPham sp : list) {
            initializeSanPhamRelations(sp);
        }
    }

    public void initializeSanPhamRelations(SanPham sp) {
        if (sp == null) return;
        try {
            if (sp.getDanhMuc() != null) {
                sp.getDanhMuc().getTenDanhMuc();
            }
            if (sp.getNhaXuatBan() != null) {
                sp.getNhaXuatBan().getTenNXB();
            }
            if (sp.getShop() != null) {
                sp.getShop().getTenShop();
                sp.getShop().getSlug();
            }
            if (sp.getSanPhamTacGias() != null) {
                sp.getSanPhamTacGias().size();
                for (var sptg : sp.getSanPhamTacGias()) {
                    if (sptg != null && sptg.getTacGia() != null) {
                        sptg.getTacGia().getTenTacGia();
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Cacheable(value = "categories", key = "#loaiSP")
    public List<DanhMuc> getCategoriesByLoaiSP(String loaiSP) {
        List<DanhMuc> allActive = danhMucRepository.findAll().stream()
                .filter(dm -> Boolean.TRUE.equals(dm.getTrangThai()))
                .toList();

        List<DanhMuc> linked = sanPhamRepository.findDistinctDanhMucByLoaiSP(loaiSP);

        if ("Sach".equalsIgnoreCase(loaiSP)) {
            return allActive.stream()
                    .filter(dm -> {
                        String name = dm.getTenDanhMuc().toLowerCase();
                        boolean isVpp = name.contains("bút") || name.contains("sổ") || name.contains("dụng cụ") || name.contains("văn phòng phẩm");
                        boolean isQuaTang = name.contains("túi") || name.contains("book nook") || name.contains("boardgame") || name.contains("quà tặng") || name.contains("nến");
                        return (!isVpp && !isQuaTang) || linked.stream().anyMatch(d -> d.getMaDanhMuc().equals(dm.getMaDanhMuc()));
                    })
                    .toList();
        } else if ("VanPhongPham".equalsIgnoreCase(loaiSP)) {
            return allActive.stream()
                    .filter(dm -> {
                        String name = dm.getTenDanhMuc().toLowerCase();
                        boolean isVpp = name.contains("bút") || name.contains("sổ") || name.contains("dụng cụ") || name.contains("văn phòng phẩm") || name.contains("vpp");
                        return isVpp || linked.stream().anyMatch(d -> d.getMaDanhMuc().equals(dm.getMaDanhMuc()));
                    })
                    .toList();
        } else if ("QuaTang".equalsIgnoreCase(loaiSP)) {
            return allActive.stream()
                    .filter(dm -> {
                        String name = dm.getTenDanhMuc().toLowerCase();
                        boolean isQuaTang = name.contains("túi") || name.contains("book nook") || name.contains("boardgame") || name.contains("quà tặng") || name.contains("nến") || name.contains("decor") || name.contains("trang trí");
                        return isQuaTang || linked.stream().anyMatch(d -> d.getMaDanhMuc().equals(dm.getMaDanhMuc()));
                    })
                    .toList();
        }
        return allActive;
    }

    // Tìm kiếm + lọc theo nhóm loạiSP (hoặc toàn bộ nếu loaiSP null/rỗng/"ALL")
    public Page<SanPham> searchAndFilter(
            String loaiSP,
            String keyword,
            Integer maDanhMuc,
            String sortBy,
            int page,
            int size) {

        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = maDanhMuc != null && maDanhMuc > 0;
        boolean hasLoaiSP = loaiSP != null && !loaiSP.isBlank() && !"ALL".equalsIgnoreCase(loaiSP);

        if (hasLoaiSP) {
            if (hasKeyword && hasCategory) {
                return sanPhamRepository.searchByLoaiSPAndDanhMucAndKeyword(loaiSP, maDanhMuc, keyword.trim(), pageable);
            } else if (hasKeyword) {
                return sanPhamRepository.searchByLoaiSPAndKeyword(loaiSP, keyword.trim(), pageable);
            } else if (hasCategory) {
                DanhMuc dm = danhMucRepository.findById(maDanhMuc).orElse(null);
                if (dm != null) {
                    return sanPhamRepository.findByLoaiSPAndDanhMucAndTrangThaiIn(loaiSP, dm, CUSTOMER_STATUSES, pageable);
                }
            }
            return sanPhamRepository.findByLoaiSPAndTrangThaiIn(loaiSP, CUSTOMER_STATUSES, pageable);
        } else {
            if (hasKeyword && hasCategory) {
                return sanPhamRepository.searchAllByDanhMucAndKeyword(keyword.trim(), maDanhMuc, pageable);
            } else if (hasKeyword) {
                return sanPhamRepository.searchAllByKeyword(keyword.trim(), pageable);
            } else if (hasCategory) {
                DanhMuc dm = danhMucRepository.findById(maDanhMuc).orElse(null);
                if (dm != null) {
                    return sanPhamRepository.findByDanhMucAndTrangThaiIn(dm, CUSTOMER_STATUSES, pageable);
                }
            }
            return sanPhamRepository.findByTrangThaiIn(CUSTOMER_STATUSES, pageable);
        }
    }

    public Page<SanPham> searchAndFilter(
            String loaiSP,
            String keyword,
            Integer maDanhMuc,
            Integer maTacGia,
            Integer maNXB,
            String sortBy,
            int page,
            int size) {

        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (maTacGia != null && maTacGia > 0) {
            return sanPhamRepository.findByTacGiaId(maTacGia, pageable);
        }
        if (maNXB != null && maNXB > 0) {
            return sanPhamRepository.findByNhaXuatBan_MaNXBAndTrangThaiIn(maNXB, CUSTOMER_STATUSES, pageable);
        }

        return searchAndFilter(loaiSP, keyword, maDanhMuc, sortBy, page, size);
    }

    public Page<SanPham> searchAndFilterByGroup(
            String loaiSP,
            String keyword,
            Integer maDanhMuc,
            String sortBy,
            int page,
            int size) {
        return searchAndFilter(loaiSP, keyword, maDanhMuc, sortBy, page, size);
    }

    public Optional<SanPham> getById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    public List<SanPham> getRelatedBooks(SanPham sanPham, int limit) {
        if (sanPham.getDanhMuc() == null) return List.of();
        Pageable pageable = PageRequest.of(0, limit + 1);
        Page<SanPham> p = sanPhamRepository.findByDanhMucAndTrangThaiIn(sanPham.getDanhMuc(), CUSTOMER_STATUSES, pageable);
        return p.getContent().stream()
                .filter(sp -> !sp.getMaSP().equals(sanPham.getMaSP()))
                .limit(limit)
                .toList();
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "ngayTao");
        return switch (sortBy) {
            case "price_asc"  -> Sort.by(Sort.Direction.ASC,  "giaBan");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "giaBan");
            case "name_asc"   -> Sort.by(Sort.Direction.ASC,  "tenSP");
            default           -> Sort.by(Sort.Direction.DESC, "ngayTao");
        };
    }

    /**
     * Xóa sản phẩm an toàn:
     * - Nếu đã có đơn hàng: Soft delete (chuyển sang 'DaXoa' để giữ tính toàn vẹn dữ liệu đơn hàng và lịch sử hóa đơn).
     * - Nếu chưa có đơn hàng: Hard delete (dọn dẹp sạch giỏ hàng, đánh giá, quan hệ tác giả, yêu thích... và xóa khỏi DB).
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"home_featured_books", "home_preview", "guest_top_sold", "book_of_the_month", "categories"}, allEntries = true)
    public void deleteProduct(Integer maSP) {
        SanPham sp = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với mã: " + maSP));

        // 1. Kiểm tra đơn hàng liên quan
        long orderCount = chiTietDonHangRepository.countBySanPham_MaSP(maSP);
        if (orderCount > 0) {
            // Đã phát sinh đơn hàng: chuyển trạng thái DaXoa để bảo toàn lịch sử hóa đơn
            sp.setTrangThai("DaXoa");
            sanPhamRepository.save(sp);
            try {
                chiTietGioHangRepository.deleteBySanPham_MaSP(maSP);
            } catch (Exception ignored) {}
            return;
        }

        // 2. Chưa có đơn hàng: Xóa sạch toàn bộ dữ liệu phụ thuộc
        try {
            chiTietGioHangRepository.deleteBySanPham_MaSP(maSP);
        } catch (Exception ignored) {}

        try {
            sanPhamTacGiaRepository.deleteBySanPhamId(maSP);
        } catch (Exception ignored) {}

        try {
            danhGiaRepository.deleteBySanPham_MaSP(maSP);
        } catch (Exception ignored) {}

        if (entityManager != null) {
            try {
                entityManager.createNativeQuery("DELETE FROM san_pham_yeu_thich WHERE masp = :id").setParameter("id", maSP).executeUpdate();
            } catch (Exception ignored) {}
            try {
                entityManager.createNativeQuery("DELETE FROM san_pham_da_xem WHERE masp = :id").setParameter("id", maSP).executeUpdate();
            } catch (Exception ignored) {}
            try {
                entityManager.createNativeQuery("DELETE FROM kho_hang WHERE masp = :id").setParameter("id", maSP).executeUpdate();
            } catch (Exception ignored) {}
            try {
                entityManager.createNativeQuery("DELETE FROM chi_tiet_kiem_ke WHERE masp = :id").setParameter("id", maSP).executeUpdate();
            } catch (Exception ignored) {}
            try {
                entityManager.createNativeQuery("DELETE FROM chi_tiet_phieu_nhap WHERE masp = :id").setParameter("id", maSP).executeUpdate();
            } catch (Exception ignored) {}
            try {
                entityManager.createNativeQuery("DELETE FROM chi_tiet_tra_hang WHERE masp = :id").setParameter("id", maSP).executeUpdate();
            } catch (Exception ignored) {}
        }

        // 3. Xóa cứng sản phẩm khỏi cơ sở dữ liệu
        sanPhamRepository.delete(sp);
    }
}
