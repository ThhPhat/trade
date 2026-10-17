package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.SanPhamDaXem;
import vn.bookstore.the4bookstore.entity.SanPhamDaXemId;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SanPhamDaXemRepository extends JpaRepository<SanPhamDaXem, SanPhamDaXemId> {
    List<SanPhamDaXem> findTop10ByKhachHangOrderByThoiGianXemDesc(KhachHang khachHang);

    // Lấy top 10 sản phẩm xem gần đây trong khoảng thời gian hiệu lực (ví dụ trong vòng 1 tiếng)
    List<SanPhamDaXem> findTop10ByKhachHangAndThoiGianXemAfterOrderByThoiGianXemDesc(KhachHang khachHang, LocalDateTime after);

    boolean existsByKhachHangAndSanPham(KhachHang khachHang, SanPham sanPham);

    // Tự động dọn dẹp các bản ghi quá hạn (quá 1 tiếng)
    @Modifying
    @Query("DELETE FROM SanPhamDaXem s WHERE s.thoiGianXem < :cutoff")
    int deleteByThoiGianXemBefore(@Param("cutoff") LocalDateTime cutoff);
}
