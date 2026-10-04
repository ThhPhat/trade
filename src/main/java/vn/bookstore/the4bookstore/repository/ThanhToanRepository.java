package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.ThanhToan;


public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    java.util.Optional<ThanhToan> findFirstByDonHangOrderByMaThanhToanDesc(vn.bookstore.the4bookstore.entity.DonHang donHang);
}
