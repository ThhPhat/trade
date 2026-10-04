package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.KhuyenMai;
import vn.bookstore.the4bookstore.entity.VoucherDaLuu;

import java.util.List;
import java.util.Optional;


public interface VoucherDaLuuRepository extends JpaRepository<VoucherDaLuu, Long> {

    /** Kiểm tra user đã lưu voucher này chưa */
    boolean existsByKhachHangAndKhuyenMai(KhachHang khachHang, KhuyenMai khuyenMai);

    /** Tìm bản ghi lưu voucher cụ thể của user */
    Optional<VoucherDaLuu> findByKhachHangAndKhuyenMai(KhachHang khachHang, KhuyenMai khuyenMai);

    /** Lấy tất cả voucher đã lưu của 1 user (sắp xếp mới nhất trước) */
    List<VoucherDaLuu> findByKhachHangOrderByNgayLuuDesc(KhachHang khachHang);

    /** Lấy voucher đã lưu nhưng chưa sử dụng */
    List<VoucherDaLuu> findByKhachHangAndTrangThaiOrderByNgayLuuDesc(KhachHang khachHang, String trangThai);

    /** Đếm số voucher user đã lưu */
    long countByKhachHang(KhachHang khachHang);

    /** Đếm số voucher chưa dùng */
    long countByKhachHangAndTrangThai(KhachHang khachHang, String trangThai);

    /** Đếm số lần user đã dùng 1 voucher cụ thể */
    long countByKhachHangAndKhuyenMaiAndTrangThai(KhachHang khachHang, KhuyenMai khuyenMai, String trangThai);

    /** Lấy danh sách maKM mà user đã lưu (để filter nhanh trên homepage) */
    @Query("SELECT v.khuyenMai.maKM FROM VoucherDaLuu v WHERE v.khachHang = :kh")
    List<Integer> findSavedVoucherIdsByKhachHang(@Param("kh") KhachHang kh);
}
