package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.TaiKhoan;


public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    java.util.Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    java.util.Optional<TaiKhoan> findByEmail(String email);
    java.util.Optional<TaiKhoan> findByProviderId(String providerId);
}

