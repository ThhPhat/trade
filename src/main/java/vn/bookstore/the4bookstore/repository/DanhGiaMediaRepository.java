package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.DanhGia;
import vn.bookstore.the4bookstore.entity.DanhGiaMedia;

import java.util.List;

@Repository
public interface DanhGiaMediaRepository extends JpaRepository<DanhGiaMedia, Integer> {
    List<DanhGiaMedia> findByDanhGia(DanhGia danhGia);
}
