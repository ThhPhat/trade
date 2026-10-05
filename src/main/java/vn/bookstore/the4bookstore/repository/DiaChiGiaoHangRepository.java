package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.DiaChiGiaoHang;
import vn.bookstore.the4bookstore.entity.KhachHang;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiGiaoHangRepository extends JpaRepository<DiaChiGiaoHang, Integer> {
    List<DiaChiGiaoHang> findByKhachHangOrderByLaMacDinhDescNgayTaoDesc(KhachHang khachHang);
    Optional<DiaChiGiaoHang> findByKhachHangAndLaMacDinhTrue(KhachHang khachHang);
}
