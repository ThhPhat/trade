package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.*;
import vn.bookstore.the4bookstore.service.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ProductController.class, GlobalControllerAdvice.class})
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private SanPhamService sanPhamService;
    @MockitoBean private DanhMucService danhMucService;
    @MockitoBean private TacGiaService tacGiaService;
    @MockitoBean private NhaXuatBanService nhaXuatBanService;
    @MockitoBean private ShopService shopService;

    @MockitoBean private SanPhamRepository sanPhamRepository;
    @MockitoBean private DanhMucRepository danhMucRepository;
    @MockitoBean private TaiKhoanRepository taiKhoanRepository;
    @MockitoBean private KhachHangRepository khachHangRepository;
    @MockitoBean private UserInteractionService userInteractionService;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    @MockitoBean private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private CustomOidcUserService customOidcUserService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private DanhMuc catSach;
    private DanhMuc catVpp;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        catSach = new DanhMuc();
        catSach.setMaDanhMuc(1);
        catSach.setTenDanhMuc("Văn học");
        catSach.setTrangThai(true);

        catVpp = new DanhMuc();
        catVpp.setMaDanhMuc(2);
        catVpp.setTenDanhMuc("Bút viết");
        catVpp.setTrangThai(true);

        when(sanPhamService.getCategoriesByLoaiSP("Sach")).thenReturn(List.of(catSach));
        when(sanPhamService.getCategoriesByLoaiSP("VanPhongPham")).thenReturn(List.of(catVpp));
        when(sanPhamService.getCategoriesByLoaiSP("QuaTang")).thenReturn(List.of());
        when(tacGiaService.getAll()).thenReturn(List.of());
        when(nhaXuatBanService.getAll()).thenReturn(List.of());
        when(userInteractionService.getReviewsForProduct(any())).thenReturn(List.of());
        when(userInteractionService.getAverageRating(any())).thenReturn(0.0);
    }

    @Test
    void landing_WithoutLoaiSP_ShouldResetDanhMuc() throws Exception {
        // Given: User passes danhMuc=1 and q=test without selecting loaiSP
        when(sanPhamService.searchAndFilter(isNull(), eq("test"), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/san-pham")
                        .param("q", "test")
                        .param("danhMuc", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attribute("selectedDanhMuc", (Object) null));

        verify(sanPhamService).searchAndFilter(isNull(), eq("test"), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt());
    }

    @Test
    void landing_WithValidLoaiSPAndValidDanhMuc_ShouldKeepDanhMuc() throws Exception {
        // Given: loaiSP=Sach and danhMuc=1 (which belongs to Sach)
        when(sanPhamService.searchAndFilter(eq("Sach"), nullable(String.class), eq(1), isNull(), isNull(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/san-pham")
                        .param("loaiSP", "Sach")
                        .param("danhMuc", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attribute("selectedDanhMuc", 1));

        verify(sanPhamService).searchAndFilter(eq("Sach"), nullable(String.class), eq(1), isNull(), isNull(), anyString(), anyInt(), anyInt());
    }

    @Test
    void landing_WithMismatchedLoaiSPAndDanhMuc_ShouldResetDanhMuc() throws Exception {
        // Given: loaiSP=VanPhongPham but danhMuc=1 (which belongs to Sach, not VPP)
        when(sanPhamService.searchAndFilter(eq("VanPhongPham"), nullable(String.class), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        // When & Then: danhMuc must be reset to null
        mockMvc.perform(get("/san-pham")
                        .param("loaiSP", "VanPhongPham")
                        .param("danhMuc", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attribute("selectedDanhMuc", (Object) null));

        verify(sanPhamService).searchAndFilter(eq("VanPhongPham"), nullable(String.class), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt());
    }

    @Test
    void sachList_WithInvalidDanhMuc_ShouldResetDanhMuc() throws Exception {
        // Given: /san-pham/sach with danhMuc=2 (which is VPP, not Sach)
        when(sanPhamService.searchAndFilter(eq("Sach"), anyString(), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/san-pham/sach")
                        .param("danhMuc", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attribute("selectedDanhMuc", (Object) null));

        verify(sanPhamService).searchAndFilter(eq("Sach"), anyString(), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt());
    }

    @Test
    void productDetail_WithHetHangProduct_ShouldDisplayDetail() throws Exception {
        // Given: Product with trangThai = "HetHang" and soLuongTon = 0
        SanPham outOfStock = new SanPham();
        outOfStock.setMaSP(99);
        outOfStock.setTenSP("Sách Hết Hàng");
        outOfStock.setTrangThai("HetHang");
        outOfStock.setSoLuongTon(0);
        outOfStock.setGiaBan(100000);
        outOfStock.setDanhMuc(catSach);

        when(sanPhamService.getById(99)).thenReturn(Optional.of(outOfStock));
        when(sanPhamService.getRelatedBooks(any(), anyInt())).thenReturn(List.of());

        // When & Then: Out of stock product must still be displayed to customer
        mockMvc.perform(get("/san-pham/99"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attribute("sanPham", outOfStock));
    }

    @Test
    void productDetail_WithLockedProduct_ShouldRedirectWithError() throws Exception {
        // Given: Product with trangThai = "NgungBan" (locked)
        SanPham lockedProduct = new SanPham();
        lockedProduct.setMaSP(100);
        lockedProduct.setTenSP("Sách Bị Khóa");
        lockedProduct.setTrangThai("NgungBan");
        lockedProduct.setDanhMuc(catSach);

        when(sanPhamService.getById(100)).thenReturn(Optional.of(lockedProduct));

        // When & Then: Locked product must NOT be accessible to customer, redirecting with error
        mockMvc.perform(get("/san-pham/100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/san-pham"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
