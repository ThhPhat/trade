package vn.bookstore.the4bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.entity.TaiKhoan;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Optional<Shop> findByTaiKhoan(TaiKhoan taiKhoan);
    Optional<Shop> findBySlug(String slug);
    boolean existsByTenShop(String tenShop);
    boolean existsBySlug(String slug);
    long countByTrangThai(String trangThai);
    List<Shop> findByTrangThai(String trangThai);
    Page<Shop> findByTrangThai(String trangThai, Pageable pageable);
    Page<Shop> findByTrangThaiNot(String trangThai, Pageable pageable);
    Page<Shop> findByTenShopContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Shop> findByTenShopContainingIgnoreCaseAndTrangThaiNot(String keyword, String trangThai, Pageable pageable);
    List<Shop> findByTenShopContainingIgnoreCaseAndTrangThai(String keyword, String trangThai);

    @org.springframework.data.jpa.repository.Query(value = "SELECT dbo.fn_DemSachCuaShop(:maShop)", nativeQuery = true)
    Integer countActiveBooksByShop(@org.springframework.data.repository.query.Param("maShop") Integer maShop);

    @org.springframework.data.jpa.repository.Query("""
        SELECT DISTINCT s FROM Shop s
        LEFT JOIN s.taiKhoan tk
        LEFT JOIN SanPham sp ON sp.shop = s AND sp.trangThai != 'DaXoa'
        WHERE s.trangThai != 'DaXoa'
          AND (
            (:searchType = 'all' AND (
                LOWER(s.tenShop) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(tk.tenDangNhap) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.emailShop) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(sp.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ))
            OR (:searchType = 'shopName' AND LOWER(s.tenShop) LIKE LOWER(CONCAT('%', :keyword, '%')))
            OR (:searchType = 'productName' AND LOWER(sp.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')))
            OR (:searchType = 'owner' AND (LOWER(tk.tenDangNhap) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            OR (:searchType = 'phone' AND LOWER(s.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')))
            OR (:searchType = 'email' AND LOWER(s.emailShop) LIKE LOWER(CONCAT('%', :keyword, '%')))
          )
    """)
    Page<Shop> searchShopsByCriteria(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("searchType") String searchType,
            Pageable pageable
    );
}
