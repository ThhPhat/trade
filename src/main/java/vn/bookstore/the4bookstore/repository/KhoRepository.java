package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.Kho;

import java.util.Optional;


public interface KhoRepository extends JpaRepository<Kho, Integer> {
    Optional<Kho> findByTenKho(String tenKho);
}
