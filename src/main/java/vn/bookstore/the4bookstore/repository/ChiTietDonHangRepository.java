package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.bookstore.the4bookstore.entity.ChiTietDonHang;
import vn.bookstore.the4bookstore.entity.ChiTietDonHangId;


public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangId> {

    @Query(value = "SELECT fn_TongSachDaBan()", nativeQuery = true)
    Long getTotalBooksSold();

    @Query("SELECT DISTINCT ct.donHang.maDH FROM ChiTietDonHang ct WHERE ct.sanPham.maSP = :maSP AND ct.donHang.khachHang.maKH = :maKH AND ct.donHang.trangThai = 'DaGiao'")
    java.util.List<Integer> findDeliveredOrderIdsByCustomerAndProduct(@org.springframework.data.repository.query.Param("maKH") Integer maKH, @org.springframework.data.repository.query.Param("maSP") Integer maSP);

    @Query("SELECT COUNT(ct) FROM ChiTietDonHang ct WHERE ct.sanPham.maSP = :maSP")
    long countBySanPham_MaSP(@org.springframework.data.repository.query.Param("maSP") Integer maSP);
}

