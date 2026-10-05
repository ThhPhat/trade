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

    @PersistenceContext
    private EntityManager entityManager;

    public List<SanPham> getFeaturedBooks() {
        List<DanhMuc> bookCategories = getCategoriesByLoaiSP("Sach");
        List<Integer> bookCatIds = bookCategories.stream().map(DanhMuc::getMaDanhMuc).toList();

        Pageable pageable = PageRequest.of(0, 8);
        if (!bookCatIds.isEmpty()) {
            List<SanPham> books = sanPhamRepository.findTopBooksByCategoriesAndTrangThais(CUSTOMER_STATUSES, bookCatIds, pageable);
            if (!books.isEmpty()) {
                return books;
            }
        }
        return sanPhamRepository.findTopBooksByTrangThais(CUSTOMER_STATUSES, pageable);
    }

    public List<SanPham> getPreviewByLoaiSP(String loaiSP) {
        return sanPhamRepository.findTop4ByLoaiSPAndTrangThaiIn(loaiSP, CUSTOMER_STATUSES);
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
