package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.ChiTietPhieuNhap;
import vn.bookstore.the4bookstore.entity.ChiTietPhieuNhapId;


public interface ChiTietPhieuNhapRepository extends JpaRepository<ChiTietPhieuNhap, ChiTietPhieuNhapId> {
    java.util.List<ChiTietPhieuNhap> findByPhieuNhap(vn.bookstore.the4bookstore.entity.PhieuNhap phieuNhap);
}

