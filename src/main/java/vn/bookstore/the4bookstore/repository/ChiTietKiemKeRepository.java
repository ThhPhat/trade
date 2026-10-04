package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.ChiTietKiemKe;
import vn.bookstore.the4bookstore.entity.ChiTietKiemKeId;


public interface ChiTietKiemKeRepository extends JpaRepository<ChiTietKiemKe, ChiTietKiemKeId> {
    java.util.List<ChiTietKiemKe> findByPhieuKiemKe(vn.bookstore.the4bookstore.entity.PhieuKiemKe phieuKiemKe);
}

