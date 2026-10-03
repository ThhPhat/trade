package vn.bookstore.the4bookstore.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.DanhMuc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query(value = "SELECT * FROM san_pham WHERE masp = :id FOR UPDATE", nativeQuery = true)
    Optional<SanPham> findByIdWithLock(@Param("id") Integer id);

    @Modifying
    @Query(value = "UPDATE san_pham SET so_luong_ton = so_luong_ton + :soLuong WHERE masp = :maSP", nativeQuery = true)
    int increaseStock(@Param("maSP") Integer maSP, @Param("soLuong") Integer soLuong);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"danhMuc", "nhaXuatBan", "nhaCungCap"})
    List<SanPham> findAll();
    
    List<SanPham> findTop8ByTrangThaiOrderByNgayTaoDesc(String trangThai);
    List<SanPham> findTop8ByTrangThaiInOrderByNgayTaoDesc(Collection<String> trangThais);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.trangThai = :trangThai " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByTrangThai(@Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.trangThai IN :trangThais " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByTrangThais(@Param("trangThais") Collection<String> trangThais, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.danhMuc.maDanhMuc IN :categoryIds " +
           "AND s.trangThai = :trangThai " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByCategoriesAndTrangThai(@Param("trangThai") String trangThai,
                                                      @Param("categoryIds") List<Integer> categoryIds,
                                                      Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE (LOWER(s.loaiSP) = 'sach' OR s.loaiSP IS NULL) " +
           "AND s.danhMuc.maDanhMuc IN :categoryIds " +
           "AND s.trangThai IN :trangThais " +
           "ORDER BY s.ngayTao DESC")
    List<SanPham> findTopBooksByCategoriesAndTrangThais(@Param("trangThais") Collection<String> trangThais,
                                                      @Param("categoryIds") List<Integer> categoryIds,
                                                      Pageable pageable);

    List<SanPham> findTop4ByLoaiSPAndTrangThai(String loaiSP, String trangThai);
    List<SanPham> findTop4ByLoaiSPAndTrangThaiIn(String loaiSP, Collection<String> trangThais);

    Page<SanPham> findByLoaiSPAndTrangThai(String loaiSP, String trangThai, Pageable pageable);
    Page<SanPham> findByLoaiSPAndTrangThaiIn(String loaiSP, Collection<String> trangThais, Pageable pageable);

    Page<SanPham> findByLoaiSPAndDanhMucAndTrangThai(String loaiSP, DanhMuc danhMuc, String trangThai, Pageable pageable);
    Page<SanPham> findByLoaiSPAndDanhMucAndTrangThaiIn(String loaiSP, DanhMuc danhMuc, Collection<String> trangThais, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.loaiSP = :loaiSP AND s.trangThai IN ('DangBan', 'HetHang') " +
           "AND (LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchByLoaiSPAndKeyword(@Param("loaiSP") String loaiSP,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.loaiSP = :loaiSP AND s.trangThai IN ('DangBan', 'HetHang') " +
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
    Page<SanPham> findByTrangThai(String trangThai, Pageable pageable);
    Page<SanPham> findByTrangThaiIn(Collection<String> trangThais, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.trangThai IN ('DangBan', 'HetHang') AND (" +
           "LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN s.nhaXuatBan nxb " +
           "WHERE s.trangThai IN ('DangBan', 'HetHang') AND s.danhMuc.maDanhMuc = :maDanhMuc AND (" +
           "LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.ISBN) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(nxb.tenNXB) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT 1 FROM SanPhamTacGia sptg WHERE sptg.sanPham = s AND LOWER(sptg.tacGia.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<SanPham> searchAllByDanhMucAndKeyword(@Param("keyword") String keyword,
                                               @Param("maDanhMuc") Integer maDanhMuc,
                                               Pageable pageable);

    Page<SanPham> findByDanhMucAndTrangThai(DanhMuc danhMuc, String trangThai, Pageable pageable);
    Page<SanPham> findByDanhMucAndTrangThaiIn(DanhMuc danhMuc, Collection<String> trangThais, Pageable pageable);

    @Query("SELECT DISTINCT s.danhMuc FROM SanPham s WHERE s.loaiSP = :loaiSP AND s.trangThai IN ('DangBan', 'HetHang') AND s.danhMuc IS NOT NULL")
    List<DanhMuc> findDistinctDanhMucByLoaiSP(@Param("loaiSP") String loaiSP);

    long countByDanhMuc_MaDanhMuc(Integer maDanhMuc);

    long countByNhaXuatBan_MaNXB(Integer maNXB);

    @Query(value = "SELECT fn_DemSanPhamSapHet()", nativeQuery = true)
    long countLowStock();

    @Query(value = "SELECT fn_DemSanPhamHetHang()", nativeQuery = true)
    long countOutOfStock();

    List<SanPham> findByNhaXuatBan_MaNXB(Integer maNXB);

    @Query("SELECT sptg.sanPham FROM SanPhamTacGia sptg WHERE sptg.tacGia.maTacGia = :maTacGia")
    List<SanPham> findBooksByTacGiaId(@Param("maTacGia") Integer maTacGia);

    @Query("SELECT DISTINCT s FROM SanPham s JOIN s.sanPhamTacGias sptg WHERE sptg.tacGia.maTacGia = :maTacGia AND s.trangThai IN ('DangBan', 'HetHang')")
    Page<SanPham> findByTacGiaId(@Param("maTacGia") Integer maTacGia, Pageable pageable);

    Page<SanPham> findByNhaXuatBan_MaNXBAndTrangThai(Integer maNXB, String trangThai, Pageable pageable);
    Page<SanPham> findByNhaXuatBan_MaNXBAndTrangThaiIn(Integer maNXB, Collection<String> trangThais, Pageable pageable);
}
