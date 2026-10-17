package vn.bookstore.the4bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.PhieuKiemKe;

import java.util.List;

public interface PhieuKiemKeRepository extends JpaRepository<PhieuKiemKe, Integer> {
    List<PhieuKiemKe> findByTrangThai(String trangThai);
    List<PhieuKiemKe> findByShop_MaShopOrderByNgayKiemKeDesc(Integer maShop);
    Page<PhieuKiemKe> findByShop_MaShopOrderByNgayKiemKeDesc(Integer maShop, Pageable pageable);

    // ====================================================================
    // ĐỀ TÀI DBMS330284: GỌI STORED PROCEDURE DUYỆT KIỂM KÊ CỦA SHOP
    // ====================================================================
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "EXEC sp_ShopXacNhanKiemKe :maPhieuKiemKe, :maShop")
    void executeSpShopXacNhanKiemKe(@Param("maPhieuKiemKe") Integer maPhieuKiemKe, @Param("maShop") Integer maShop);
}
