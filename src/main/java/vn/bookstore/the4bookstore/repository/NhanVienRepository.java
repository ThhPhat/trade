package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.NhanVien;


public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
}
