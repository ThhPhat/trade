package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.Kho;
import vn.bookstore.the4bookstore.entity.PhieuNhap;

import java.time.LocalDateTime;
import java.util.List;


public interface PhieuNhapRepository extends JpaRepository<PhieuNhap, Integer> {
    List<PhieuNhap> findByKho(Kho kho);
    List<PhieuNhap> findByTrangThai(String trangThai);
    List<PhieuNhap> findByNgayNhapBetween(LocalDateTime tuNgay, LocalDateTime denNgay);
}
