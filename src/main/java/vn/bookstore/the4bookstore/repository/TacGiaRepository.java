package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.TacGia;

import java.util.Optional;
import java.util.List;


public interface TacGiaRepository extends JpaRepository<TacGia, Integer> {
    Optional<TacGia> findByTenTacGia(String tenTacGia);
    List<TacGia> findByTenTacGiaContainingIgnoreCase(String keyword);
}
