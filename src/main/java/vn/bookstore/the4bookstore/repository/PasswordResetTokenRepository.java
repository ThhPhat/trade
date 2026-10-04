package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.PasswordResetToken;
import vn.bookstore.the4bookstore.entity.TaiKhoan;

import java.util.List;
import java.util.Optional;


public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByTaiKhoanAndOtpCodeAndUsedFalseOrderByNgayTaoDesc(TaiKhoan taiKhoan, String otpCode);

    Optional<PasswordResetToken> findFirstByTaiKhoanAndUsedFalseOrderByNgayTaoDesc(TaiKhoan taiKhoan);

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findAllByTaiKhoan(TaiKhoan taiKhoan);

    void deleteByTaiKhoan(TaiKhoan taiKhoan);
}
