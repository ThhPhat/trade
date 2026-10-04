package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.GioHang;
import vn.bookstore.the4bookstore.entity.KhachHang;

import java.util.Optional;


public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByKhachHang(KhachHang khachHang);
    Optional<GioHang> findByKhachHang_MaKH(Integer maKH);
}
