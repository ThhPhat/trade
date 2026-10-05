package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.SanPhamYeuThich;
import vn.bookstore.the4bookstore.entity.SanPhamYeuThichId;

import java.util.List;

@Repository
public interface SanPhamYeuThichRepository extends JpaRepository<SanPhamYeuThich, SanPhamYeuThichId> {
    List<SanPhamYeuThich> findByKhachHangOrderByNgayThichDesc(KhachHang khachHang);
    boolean existsByKhachHangAndSanPham(KhachHang khachHang, SanPham sanPham);
    void deleteByKhachHangAndSanPham(KhachHang khachHang, SanPham sanPham);
    long countBySanPham(SanPham sanPham);
}
