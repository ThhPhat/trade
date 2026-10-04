package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.NhaXuatBan;

import java.util.Optional;
import java.util.List;


public interface NhaXuatBanRepository extends JpaRepository<NhaXuatBan, Integer> {
    Optional<NhaXuatBan> findByTenNXB(String tenNXB);
    List<NhaXuatBan> findByTenNXBContainingIgnoreCase(String keyword);
}
