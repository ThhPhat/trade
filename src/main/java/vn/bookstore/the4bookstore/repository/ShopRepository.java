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

    @org.springframework.data.jpa.repository.Query(value = "SELECT fn_DemSachCuaShop(:maShop)", nativeQuery = true)
    Integer countActiveBooksByShop(@org.springframework.data.repository.query.Param("maShop") Integer maShop);
}
