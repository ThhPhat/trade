package vn.bookstore.the4bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.TaiKhoan;

import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    Optional<TaiKhoan> findByEmail(String email);
    Optional<TaiKhoan> findByProviderId(String providerId);

    @Query("SELECT t FROM TaiKhoan t WHERE (:vaiTro IS NULL OR :vaiTro = '' OR t.vaiTro = :vaiTro) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(t.tenDangNhap) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TaiKhoan> searchAccounts(@Param("keyword") String keyword, @Param("vaiTro") String vaiTro, Pageable pageable);

    long countByVaiTro(String vaiTro);
    long countByTrangThai(String trangThai);
}

