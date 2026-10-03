package vn.bookstore.the4bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.DonHang;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.KhuyenMai;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    @Query(value = "SELECT fn_TinhDoanhThu(:startDate, :endDate)", nativeQuery = true)
    Long getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT fn_DemDonHang(:startDate, :endDate)", nativeQuery = true)
    Long getOrderCountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<DonHang> findByKhachHangOrderByNgayDatDesc(KhachHang khachHang);

    Page<DonHang> findAllByOrderByNgayDatDesc(Pageable pageable);

    Page<DonHang> findByTrangThaiOrderByNgayDatDesc(String trangThai, Pageable pageable);

    Page<DonHang> findByTrangThaiInOrderByNgayDatDesc(List<String> trangThaiList, Pageable pageable);

    Long countByKhachHang(KhachHang khachHang);

    Long countByKhachHangAndTrangThai(KhachHang khachHang, String trangThai);
    
    Long countByTrangThai(String trangThai);

    Long countByTrangThaiIn(List<String> trangThaiList);

    Long countByNgayDatBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<DonHang> findByKhuyenMaiOrderByNgayDatDesc(KhuyenMai khuyenMai);

    Long countByKhuyenMai(KhuyenMai khuyenMai);

    @Query(value = "SELECT fn_TinhTienGiamKhuyenMai(:#{#km.maKM})", nativeQuery = true)
    Long sumTienGiamByKhuyenMai(@Param("km") KhuyenMai km);

    @Query(value = "SELECT fn_TinhDoanhThuKhuyenMai(:#{#km.maKM})", nativeQuery = true)
    Long sumTongTienByKhuyenMai(@Param("km") KhuyenMai km);
}

