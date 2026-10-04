package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.ChiTietTraHang;
import vn.bookstore.the4bookstore.entity.ChiTietTraHangId;


public interface ChiTietTraHangRepository extends JpaRepository<ChiTietTraHang, ChiTietTraHangId> {
}

