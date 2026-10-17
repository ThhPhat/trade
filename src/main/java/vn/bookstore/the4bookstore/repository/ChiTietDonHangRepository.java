package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.bookstore.the4bookstore.entity.ChiTietDonHang;
import vn.bookstore.the4bookstore.entity.ChiTietDonHangId;


public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangId> {

    @Query(value = "SELECT dbo.fn_TongSachDaBan()", nativeQuery = true)
    Long getTotalBooksSold();

    @Query("SELECT DISTINCT ct.donHang.maDH FROM ChiTietDonHang ct WHERE ct.sanPham.maSP = :maSP AND ct.donHang.khachHang.maKH = :maKH AND ct.donHang.trangThai IN ('DaGiao', 'HoanTat')")
    java.util.List<Integer> findDeliveredOrderIdsByCustomerAndProduct(@org.springframework.data.repository.query.Param("maKH") Integer maKH, @org.springframework.data.repository.query.Param("maSP") Integer maSP);

    @Query("SELECT DISTINCT ct.donHang.maDH FROM ChiTietDonHang ct WHERE ct.sanPham.maSP = :maSP AND ct.donHang.khachHang.maKH = :maKH AND ct.donHang.trangThai IN ('DaGiao', 'HoanTat') AND ct.donHang.maDH NOT IN (SELECT dg.donHang.maDH FROM DanhGia dg WHERE dg.sanPham.maSP = :maSP AND dg.khachHang.maKH = :maKH AND dg.donHang IS NOT NULL)")
    java.util.List<Integer> findEligibleOrderIdsForReview(@org.springframework.data.repository.query.Param("maKH") Integer maKH, @org.springframework.data.repository.query.Param("maSP") Integer maSP);

    @Query("SELECT COUNT(ct) FROM ChiTietDonHang ct WHERE ct.sanPham.maSP = :maSP")
    long countBySanPham_MaSP(@org.springframework.data.repository.query.Param("maSP") Integer maSP);

    @Query("SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietDonHang ct " +
           "WHERE ct.sanPham.maSP = :maSP " +
           "AND ct.donHang.trangThai IN ('DaGiao', 'HoanTat') " +
           "AND ct.donHang.ngayDat >= :sinceDate")
    Long countSoldQuantitySince(@org.springframework.data.repository.query.Param("maSP") Integer maSP,
                                @org.springframework.data.repository.query.Param("sinceDate") java.time.LocalDateTime sinceDate);
}

