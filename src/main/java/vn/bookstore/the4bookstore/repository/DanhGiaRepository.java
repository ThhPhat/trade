package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.DanhGia;
import vn.bookstore.the4bookstore.entity.DonHang;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.SanPham;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    List<DanhGia> findBySanPhamAndTrangThaiOrderByNgayDanhGiaDesc(SanPham sanPham, String trangThai);
    boolean existsByKhachHangAndSanPhamAndDonHang(KhachHang khachHang, SanPham sanPham, DonHang donHang);
    long countBySanPhamAndTrangThai(SanPham sanPham, String trangThai);

    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanPham = :sanPham AND d.trangThai = 'HienThi'")
    Double findAverageRatingBySanPham(@Param("sanPham") SanPham sanPham);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM DanhGia d WHERE d.sanPham.maSP = :maSP")
    void deleteBySanPham_MaSP(@Param("maSP") Integer maSP);
}
