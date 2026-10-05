package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.SanPhamDaXem;
import vn.bookstore.the4bookstore.entity.SanPhamDaXemId;

import java.util.List;

@Repository
public interface SanPhamDaXemRepository extends JpaRepository<SanPhamDaXem, SanPhamDaXemId> {
    List<SanPhamDaXem> findTop10ByKhachHangOrderByThoiGianXemDesc(KhachHang khachHang);
    boolean existsByKhachHangAndSanPham(KhachHang khachHang, SanPham sanPham);
}
