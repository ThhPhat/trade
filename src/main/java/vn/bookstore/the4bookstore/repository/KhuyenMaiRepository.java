package vn.bookstore.the4bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.bookstore.the4bookstore.entity.KhuyenMai;

import java.util.List;
import java.util.Optional;


public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer> {
    Optional<KhuyenMai> findByMaCode(String maCode);

    boolean existsByMaCode(String maCode);

    boolean existsByMaCodeAndMaKMNot(String maCode, Integer maKM);

    List<KhuyenMai> findAllByOrderByMaKMDesc();

    @Query("SELECT km FROM KhuyenMai km WHERE " +
           "(:keyword IS NULL OR LOWER(km.maCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(km.tenKM) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:loaiGiam IS NULL OR :loaiGiam = '' OR km.loaiGiam = :loaiGiam) " +
           "ORDER BY km.maKM DESC")
    List<KhuyenMai> searchPromotions(@Param("keyword") String keyword, @Param("loaiGiam") String loaiGiam);

    List<KhuyenMai> findByShop_MaShopOrderByMaKMDesc(Integer maShop);

    List<KhuyenMai> findByPhamViOrderByMaKMDesc(String phamVi);

    List<KhuyenMai> findByPhamViAndLoaiKhuyenMaiOrderByMaKMDesc(String phamVi, String loaiKhuyenMai);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE KhuyenMai km SET km.shop = null WHERE km.shop.maShop = :maShop")
    void detachShopFromPromotions(@Param("maShop") Integer maShop);
}
