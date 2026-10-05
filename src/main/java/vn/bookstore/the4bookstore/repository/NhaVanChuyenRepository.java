package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.NhaVanChuyen;

import java.util.List;

@Repository
public interface NhaVanChuyenRepository extends JpaRepository<NhaVanChuyen, Integer> {
    List<NhaVanChuyen> findByTrangThai(String trangThai);
}
