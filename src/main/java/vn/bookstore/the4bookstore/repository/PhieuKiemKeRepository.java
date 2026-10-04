package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.Kho;
import vn.bookstore.the4bookstore.entity.PhieuKiemKe;

import java.util.List;


public interface PhieuKiemKeRepository extends JpaRepository<PhieuKiemKe, Integer> {
    List<PhieuKiemKe> findByKho(Kho kho);
    List<PhieuKiemKe> findByTrangThai(String trangThai);
}
