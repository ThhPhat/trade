package vn.bookstore.the4bookstore.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.DanhMuc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query(value = "SELECT * FROM san_pham WHERE masp = :id FOR UPDATE", nativeQuery = true)
    Optional<SanPham> findByIdWithLock(@Param("id") Integer id);

    @Modifying
    @Query(value = "UPDATE san_pham SET so_luong_ton = so_luong_ton + :soLuong WHERE masp = :maSP", nativeQuery = true)
    int increaseStock(@Param("maSP") Integer maSP, @Param("soLuong") Integer soLuong);

    Optional<SanPham> findByISBN(String ISBN);
    boolean existsByISBN(String ISBN);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"danhMuc", "nhaXuatBan", "nhaCungCap"})
    List<SanPham> findAll();
    
    List<SanPham> findTop8ByTrangThaiOrderByNgayTaoDesc(String trangThai);
    List<SanPham> findTop8ByTrangThaiInOrderByNgayTaoDesc(Collection<String> trangThais);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByTrangThai(@Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByTrangThais(@Param("trangThais") Collection<String> trangThais, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.danhMuc.maDanhMuc IN :categoryIds " +
           "AND s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByCategoriesAndTrangThai(@Param("trangThai") String trangThai,
                                                      @Param("categoryIds") List<Integer> categoryIds,
                                                      Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.danhMuc.maDanhMuc IN :categoryIds " +
           "AND s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByCategoriesAndTrangThais(@Param("trangThais") Collection<String> trangThais,
                                                      @Param("categoryIds") List<Integer> categoryIds,
                                                      Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    List<SanPham> findTop4ByLoaiSPAndTrangThai(@Param("loaiSP") String loaiSP, @Param("trangThai") String trangThai);

    @Query("SELECT s FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    List<SanPham> findTop4ByLoaiSPAndTrangThaiIn(@Param("loaiSP") String loaiSP, @Param("trangThais") Collection<String> trangThais);

    @Query("SELECT s FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByLoaiSPAndTrangThai(@Param("loaiSP") String loaiSP, @Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByLoaiSPAndTrangThaiIn(@Param("loaiSP") String loaiSP, @Param("trangThais") Collection<String> trangThais, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.danhMuc = :danhMuc AND s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByLoaiSPAndDanhMucAndTrangThai(@Param("loaiSP") String loaiSP, @Param("danhMuc") DanhMuc danhMuc, @Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.danhMuc = :danhMuc AND s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByLoaiSPAndDanhMucAndTrangThaiIn(@Param("loaiSP") String loaiSP, @Param("danhMuc") DanhMuc danhMuc, @Param("trangThais") Collection<String> trangThais, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.loaiSP = :loaiSP AND s.trangThai IN ('DangBan', 'HetHang') " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') " +
           "AND (LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchByLoaiSPAndKeyword(@Param("loaiSP") String loaiSP,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.loaiSP = :loaiSP AND s.trangThai IN ('DangBan', 'HetHang') " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') " +
           "AND s.danhMuc.maDanhMuc = :maDanhMuc " +
           "AND (LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchByLoaiSPAndDanhMucAndKeyword(@Param("loaiSP") String loaiSP,
                                                      @Param("maDanhMuc") Integer maDanhMuc,
                                                      @Param("keyword") String keyword,
                                                      Pageable pageable);

    // --- Toàn bộ sản phẩm (không phân biệt loaiSP) ---
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByTrangThai(@Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByTrangThaiIn(@Param("trangThais") Collection<String> trangThais, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.trangThai IN ('DangBan', 'HetHang') " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') AND (" +
           "LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.trangThai IN ('DangBan', 'HetHang') AND s.danhMuc.maDanhMuc = :maDanhMuc " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') AND (" +
           "LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchAllByDanhMucAndKeyword(@Param("keyword") String keyword,
                                               @Param("maDanhMuc") Integer maDanhMuc,
                                               Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc AND s.trangThai = :trangThai " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByDanhMucAndTrangThai(@Param("danhMuc") DanhMuc danhMuc, @Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc AND s.trangThai IN :trangThais " +
           "AND (s.trangThaiKhoa = 'BinhThuong' OR s.trangThaiKhoa IS NULL) " +
           "AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong')")
    Page<SanPham> findByDanhMucAndTrangThaiIn(@Param("danhMuc") DanhMuc danhMuc, @Param("trangThais") Collection<String> trangThais, Pageable pageable);

    @Query("SELECT DISTINCT s.danhMuc FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.trangThai IN ('DangBan', 'HetHang') AND s.danhMuc IS NOT NULL")
    List<DanhMuc> findDistinctDanhMucByLoaiSP(@Param("loaiSP") String loaiSP);

    long countByDanhMuc_MaDanhMuc(Integer maDanhMuc);

    long countByNhaXuatBan_MaNXB(Integer maNXB);

    @Query(value = "SELECT fn_DemSanPhamSapHet()", nativeQuery = true)
    long countLowStock();

    @Query(value = "SELECT fn_DemSanPhamHetHang()", nativeQuery = true)
    long countOutOfStock();

    @Query(value = "SELECT fn_TinhDiemDanhGiaSanPham(:maSP)", nativeQuery = true)
    Double getAverageRatingByProductId(@Param("maSP") Integer maSP);

    List<SanPham> findByNhaXuatBan_MaNXB(Integer maNXB);

    @Query("SELECT sptg.sanPham FROM SanPhamTacGia sptg WHERE sptg.tacGia.maTacGia = :maTacGia")
    List<SanPham> findBooksByTacGiaId(@Param("maTacGia") Integer maTacGia);

    @Query("SELECT DISTINCT s FROM SanPham s JOIN s.sanPhamTacGias sptg WHERE sptg.tacGia.maTacGia = :maTacGia AND s.trangThai IN ('DangBan', 'HetHang')")
    Page<SanPham> findByTacGiaId(@Param("maTacGia") Integer maTacGia, Pageable pageable);

    Page<SanPham> findByNhaXuatBan_MaNXBAndTrangThai(Integer maNXB, String trangThai, Pageable pageable);
    Page<SanPham> findByNhaXuatBan_MaNXBAndTrangThaiIn(Integer maNXB, Collection<String> trangThais, Pageable pageable);

    // --- MARKETPLACE QUERIES ---

    // 1. Dành cho Guest: Sản phẩm bán trên 10 sản phẩm, sắp xếp giảm dần theo lượt bán
    @Query("SELECT s FROM SanPham s WHERE s.soLuongDaBan > 10 AND s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') ORDER BY s.soLuongDaBan DESC")
    List<SanPham> findTopProductsForGuest();

    // 2. Dành cho User: 20 sản phẩm mới nhất
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') AND (:maDanhMuc IS NULL OR s.danhMuc.maDanhMuc = :maDanhMuc) ORDER BY s.ngayTao DESC")
    Page<SanPham> findTop20Newest(@Param("maDanhMuc") Integer maDanhMuc, Pageable pageable);

    // 3. Dành cho User: 20 sản phẩm bán chạy nhất
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') AND (:maDanhMuc IS NULL OR s.danhMuc.maDanhMuc = :maDanhMuc) ORDER BY s.soLuongDaBan DESC")
    Page<SanPham> findTop20BestSelling(@Param("maDanhMuc") Integer maDanhMuc, Pageable pageable);

    // 4. Dành cho User: 20 sản phẩm đánh giá cao nhất
    @Query("SELECT s FROM SanPham s LEFT JOIN DanhGia dg ON dg.sanPham = s AND dg.trangThai = 'HienThi' " +
           "WHERE s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') AND (:maDanhMuc IS NULL OR s.danhMuc.maDanhMuc = :maDanhMuc) " +
           "GROUP BY s ORDER BY COALESCE(AVG(dg.soSao), 0) DESC")
    Page<SanPham> findTop20HighestRated(@Param("maDanhMuc") Integer maDanhMuc, Pageable pageable);

    // 5. Dành cho User: 20 sản phẩm yêu thích nhất
    @Query("SELECT s FROM SanPham s LEFT JOIN SanPhamYeuThich yt ON yt.sanPham = s " +
           "WHERE s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND (s.shop IS NULL OR s.shop.trangThai = 'HoatDong') AND (:maDanhMuc IS NULL OR s.danhMuc.maDanhMuc = :maDanhMuc) " +
           "GROUP BY s ORDER BY COUNT(yt) DESC")
    Page<SanPham> findTop20MostFavorited(@Param("maDanhMuc") Integer maDanhMuc, Pageable pageable);

    // 6. Dành cho Vendor: Sản phẩm của riêng shop (loại bỏ DaXoa)
    List<SanPham> findByShop_MaShop(Integer maShop);
    Page<SanPham> findByShop_MaShop(Integer maShop, Pageable pageable);
    Page<SanPham> findByShop_MaShopAndTenSPContainingIgnoreCase(Integer maShop, String keyword, Pageable pageable);
    long countByShop_MaShop(Integer maShop);

    @Query("SELECT s FROM SanPham s WHERE s.shop.maShop = :maShop AND s.trangThai != 'DaXoa'")
    Page<SanPham> findActiveByShop(@Param("maShop") Integer maShop, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.shop.maShop = :maShop AND s.trangThai != 'DaXoa' AND LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<SanPham> findActiveByShopAndKeyword(@Param("maShop") Integer maShop, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SanPham s WHERE s.shop.maShop = :maShop AND s.trangThai != 'DaXoa'")
    long countActiveByShop(@Param("maShop") Integer maShop);

    // 7. Dành cho Quản trị viên: Toàn bộ sản phẩm chưa xóa sắp xếp mới nhất
    @Query("SELECT s FROM SanPham s WHERE s.trangThai != 'DaXoa' ORDER BY s.maSP DESC")
    List<SanPham> findAllActiveOrderByMaSPDesc();

    // 8. Dành cho Shop Storefront: Sản phẩm đang bán của shop
    @Query("SELECT s FROM SanPham s WHERE s.shop.maShop = :maShop AND s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND s.shop.trangThai = 'HoatDong' ORDER BY s.ngayTao DESC")
    Page<SanPham> findActiveBooksByShop(@Param("maShop") Integer maShop, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.shop.maShop = :maShop AND s.trangThai = 'DangBan' AND s.trangThaiKhoa = 'BinhThuong' AND s.shop.trangThai = 'HoatDong' AND LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.ngayTao DESC")
    Page<SanPham> findActiveBooksByShopAndKeyword(@Param("maShop") Integer maShop, @Param("keyword") String keyword, Pageable pageable);
}
