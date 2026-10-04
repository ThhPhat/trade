package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.KhachHang;


public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    java.util.Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    java.util.Optional<KhachHang> findByEmail(String email);
    java.util.Optional<KhachHang> findByTaiKhoan(vn.bookstore.the4bookstore.entity.TaiKhoan taiKhoan);
}
