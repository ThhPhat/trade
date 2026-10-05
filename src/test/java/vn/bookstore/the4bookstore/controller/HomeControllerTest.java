package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.*;
import vn.bookstore.the4bookstore.service.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {HomeController.class, GlobalControllerAdvice.class})
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private SanPhamService sanPhamService;
    @MockitoBean private DanhMucService danhMucService;

    @MockitoBean private SanPhamRepository sanPhamRepository;
    @MockitoBean private DanhMucRepository danhMucRepository;
    @MockitoBean private TaiKhoanRepository taiKhoanRepository;
    @MockitoBean private KhachHangRepository khachHangRepository;
    @MockitoBean private KhuyenMaiRepository khuyenMaiRepository;
    @MockitoBean private VoucherDaLuuRepository voucherDaLuuRepository;
    @MockitoBean private UserInteractionService userInteractionService;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    @MockitoBean private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private CustomOidcUserService customOidcUserService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private List<DanhMuc> mockCategoryList;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        mockCategoryList = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            DanhMuc dm = new DanhMuc();
            dm.setMaDanhMuc(i);
            dm.setTenDanhMuc("Danh mục " + i);
            dm.setMoTa("Mô tả " + i);
            dm.setTrangThai(true);
            mockCategoryList.add(dm);
        }

        when(sanPhamService.getFeaturedBooks()).thenReturn(List.of());
        when(sanPhamService.getPreviewByLoaiSP(any())).thenReturn(List.of());
        when(sanPhamService.getCategoriesByLoaiSP(any())).thenReturn(List.of());
        when(danhMucService.getAllActive()).thenReturn(mockCategoryList);
        when(sanPhamRepository.findTopProductsForGuest()).thenReturn(List.of());
        when(khuyenMaiRepository.findAll()).thenReturn(List.of());
    }

    @Test
    void home_DefaultPage_ShouldReturnAllSectionsAndPaginationModel() throws Exception {
        List<DanhMuc> firstPageItems = mockCategoryList.subList(0, 10);
        Page<DanhMuc> page0 = new PageImpl<>(firstPageItems, PageRequest.of(0, 10), 15);
        when(danhMucService.getAllActivePaged(eq(0), eq(10))).thenReturn(page0);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attributeExists("featuredBooks"))
                .andExpect(model().attributeExists("sachPreview"))
                .andExpect(model().attributeExists("vppPreview"))
                .andExpect(model().attributeExists("quaTangPreview"))
                .andExpect(model().attributeExists("categoriesSach"))
                .andExpect(model().attributeExists("categoriesVpp"))
                .andExpect(model().attributeExists("categoriesQuaTang"))
                .andExpect(model().attribute("catCurrentPage", 0))
                .andExpect(model().attribute("catTotalPages", 2))
                .andExpect(model().attribute("catTotalElements", 15L))
                .andExpect(content().string(containsString("Sách Tuyển Chọn Bán Chạy")))
                .andExpect(content().string(containsString("Tất Cả Sản Phẩm")))
                .andExpect(content().string(containsString("Các danh mục")));
    }

    @Test
    void home_WithCatPage1_ShouldReturnSecondPageCategories() throws Exception {
        List<DanhMuc> secondPageItems = mockCategoryList.subList(10, 15);
        Page<DanhMuc> page1 = new PageImpl<>(secondPageItems, PageRequest.of(1, 10), 15);
        when(danhMucService.getAllActivePaged(eq(1), eq(10))).thenReturn(page1);

        mockMvc.perform(get("/").param("catPage", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attribute("catCurrentPage", 1))
                .andExpect(model().attribute("catTotalPages", 2))
                .andExpect(model().attribute("catTotalElements", 15L));
    }
}
