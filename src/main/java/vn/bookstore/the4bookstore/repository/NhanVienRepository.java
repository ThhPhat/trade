package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.NhanVien;
import vn.bookstore.the4bookstore.entity.TaiKhoan;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByTaiKhoan(TaiKhoan taiKhoan);
}
