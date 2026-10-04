package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.SanPhamTacGia;
import vn.bookstore.the4bookstore.entity.SanPhamTacGiaId;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface SanPhamTacGiaRepository extends JpaRepository<SanPhamTacGia, SanPhamTacGiaId> {
    List<SanPhamTacGia> findByTacGia_MaTacGia(Integer maTacGia);
    List<SanPhamTacGia> findBySanPham_MaSP(Integer maSP);
    long countByTacGia_MaTacGia(Integer maTacGia);

    @Transactional
    @Modifying
    @Query("DELETE FROM SanPhamTacGia sptg WHERE sptg.sanPham.maSP = :maSP")
    void deleteBySanPhamId(@Param("maSP") Integer maSP);
}

