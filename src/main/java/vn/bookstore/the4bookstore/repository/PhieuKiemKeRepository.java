package vn.bookstore.the4bookstore.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.bookstore.the4bookstore.entity.Kho;
import vn.bookstore.the4bookstore.entity.PhieuKiemKe;

import java.util.List;


public interface PhieuKiemKeRepository extends JpaRepository<PhieuKiemKe, Integer> {
    default List<PhieuKiemKe> findByKho(Kho kho) { return java.util.List.of(); }
    List<PhieuKiemKe> findByTrangThai(String trangThai);
    List<PhieuKiemKe> findByShop_MaShopOrderByNgayKiemKeDesc(Integer maShop);
    org.springframework.data.domain.Page<PhieuKiemKe> findByShop_MaShopOrderByNgayKiemKeDesc(Integer maShop, org.springframework.data.domain.Pageable pageable);

    // ====================================================================
    // ĐỀ TÀI DBMS330284: GỌI STORED PROCEDURE DUYỆT KIỂM KÊ CỦA SHOP
    // ====================================================================
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(nativeQuery = true, value = "EXEC sp_ShopXacNhanKiemKe :maPhieuKiemKe, :maShop")
    void executeSpShopXacNhanKiemKe(@org.springframework.data.repository.query.Param("maPhieuKiemKe") Integer maPhieuKiemKe, @org.springframework.data.repository.query.Param("maShop") Integer maShop);
}
