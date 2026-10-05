package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.ChiTietGioHang;
import vn.bookstore.the4bookstore.entity.ChiTietGioHangId;
import vn.bookstore.the4bookstore.entity.GioHang;
import vn.bookstore.the4bookstore.entity.SanPham;

import java.util.List;
import java.util.Optional;


public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, ChiTietGioHangId> {
    List<ChiTietGioHang> findByGioHang(GioHang gioHang);
    Optional<ChiTietGioHang> findByGioHangAndSanPham(GioHang gioHang, SanPham sanPham);
    void deleteByGioHang(GioHang gioHang);
    long countByGioHang(GioHang gioHang);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ChiTietGioHang c WHERE c.sanPham.maSP = :maSP")
    void deleteBySanPham_MaSP(@org.springframework.data.repository.query.Param("maSP") Integer maSP);
}
