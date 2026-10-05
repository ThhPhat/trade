package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AdminUserController.class, AdminImportExportController.class, GlobalControllerAdvice.class})
@Import(SecurityConfig.class)
class AdminUserAndImportExportTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private TaiKhoanRepository taiKhoanRepository;
    @MockitoBean private KhachHangRepository khachHangRepository;
    @MockitoBean private NhanVienRepository nhanVienRepository;
    @MockitoBean private ShopRepository shopRepository;
    @MockitoBean private SanPhamRepository sanPhamRepository;
    @MockitoBean private DonHangRepository donHangRepository;
    @MockitoBean private DanhMucRepository danhMucRepository;
    @MockitoBean private NhaXuatBanRepository nhaXuatBanRepository;
    @MockitoBean private PasswordEncoder passwordEncoder;

    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    @MockitoBean private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private CustomOidcUserService customOidcUserService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testListUsersReturnsOk() throws Exception {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTaiKhoan(1);
        tk.setTenDangNhap("seller1");
        tk.setEmail("seller1@example.com");
        tk.setVaiTro("VENDOR");
        tk.setTrangThai("HoatDong");

        when(taiKhoanRepository.searchAccounts(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tk)));
        when(taiKhoanRepository.count()).thenReturn(10L);
        when(taiKhoanRepository.countByVaiTro(anyString())).thenReturn(2L);
        when(taiKhoanRepository.countByTrangThai(anyString())).thenReturn(1L);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users", "totalAccounts"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testToggleLockUserSuccess() throws Exception {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTaiKhoan(2);
        tk.setTenDangNhap("user_test");
        tk.setEmail("test@example.com");
        tk.setTrangThai("HoatDong");

        when(taiKhoanRepository.findById(2)).thenReturn(Optional.of(tk));

        mockMvc.perform(post("/admin/users/2/toggle-lock").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testChangeRoleSuccess() throws Exception {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTaiKhoan(3);
        tk.setTenDangNhap("staff_user");
        tk.setEmail("staff@example.com");
        tk.setVaiTro("USER");

        when(taiKhoanRepository.findById(3)).thenReturn(Optional.of(tk));

        mockMvc.perform(post("/admin/users/3/change-role")
                        .param("newRole", "MANAGER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testImportExportDashboardOk() throws Exception {
        when(sanPhamRepository.count()).thenReturn(100L);
        when(donHangRepository.count()).thenReturn(50L);
        when(taiKhoanRepository.count()).thenReturn(20L);
        when(shopRepository.count()).thenReturn(5L);

        mockMvc.perform(get("/admin/import-export"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/import_export"))
                .andExpect(model().attribute("totalProducts", 100L));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDownloadSampleCsv() throws Exception {
        mockMvc.perform(get("/admin/import-export/sample/products"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"mau_nhap_san_pham.csv\""));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testImportProductsCsv() throws Exception {
        String csvContent = "ISBN,TenSP,GiaBan,SoLuongTon,LoaiSP\n" +
                "9780001112223,Sách Test Import,99000,10,Sach\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        Shop shop = new Shop();
        shop.setMaShop(1);
        when(shopRepository.findById(1)).thenReturn(Optional.of(shop));

        mockMvc.perform(multipart("/admin/import-export/import/products").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/import-export"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
