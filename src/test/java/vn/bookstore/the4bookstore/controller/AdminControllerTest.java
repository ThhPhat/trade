package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.bookstore.the4bookstore.entity.NhaXuatBan;
import vn.bookstore.the4bookstore.entity.TacGia;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.*;
import vn.bookstore.the4bookstore.service.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AdminController.class, GlobalControllerAdvice.class})
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private SanPhamRepository sanPhamRepository;
    @MockitoBean private DanhMucRepository danhMucRepository;
    @MockitoBean private NhaXuatBanRepository nhaXuatBanRepository;
    @MockitoBean private NhaCungCapRepository nhaCungCapRepository;
    @MockitoBean private TacGiaRepository tacGiaRepository;
    @MockitoBean private SanPhamTacGiaRepository sanPhamTacGiaRepository;
    @MockitoBean private DonHangRepository donHangRepository;

    @MockitoBean private KhoService khoService;
    @MockitoBean private ReportService reportService;
    @MockitoBean private OrderService orderService;
    @MockitoBean private TacGiaService tacGiaService;
    @MockitoBean private NhaXuatBanService nhaXuatBanService;

    @MockitoBean private TaiKhoanRepository taiKhoanRepository;
    @MockitoBean private KhachHangRepository khachHangRepository;
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
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAuthorsPageRendersWithoutTemplateError() throws Exception {
        TacGia tg = new TacGia();
        tg.setMaTacGia(1);
        tg.setTenTacGia("Matt Haig");
        tg.setMoTa("Tác giả văn học");

        when(tacGiaRepository.findAll()).thenReturn(List.of(tg));
        when(sanPhamTacGiaRepository.countByTacGia_MaTacGia(1)).thenReturn(3L);

        mockMvc.perform(get("/admin/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/authors"))
                .andExpect(model().attributeExists("authors", "bookCounts", "totalAuthors", "authorsWithBooks"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSaveAuthor() throws Exception {
        mockMvc.perform(post("/admin/authors/save")
                        .with(csrf())
                        .param("tenTacGia", "James Clear")
                        .param("moTa", "Tác giả Atomic Habits"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/authors"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteAuthor() throws Exception {
        when(sanPhamTacGiaRepository.countByTacGia_MaTacGia(1)).thenReturn(0L);

        mockMvc.perform(post("/admin/authors/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/authors"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPublishersPageRendersWithoutTemplateError() throws Exception {
        NhaXuatBan nxb = new NhaXuatBan();
        nxb.setMaNXB(1);
        nxb.setTenNXB("NXB Trẻ");
        nxb.setDiaChi("161B Lý Chính Thắng, Q3, TP.HCM");
        nxb.setEmail("hopthu@nxbtre.com.vn");
        nxb.setSoDienThoai("02839316289");

        when(nhaXuatBanRepository.findAll()).thenReturn(List.of(nxb));
        when(sanPhamRepository.countByNhaXuatBan_MaNXB(1)).thenReturn(5L);

        mockMvc.perform(get("/admin/publishers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/publishers"))
                .andExpect(model().attributeExists("publishers", "bookCounts", "totalPublishers", "publishersWithBooks"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSavePublisher() throws Exception {
        mockMvc.perform(post("/admin/publishers/save")
                        .with(csrf())
                        .param("tenNXB", "NXB Kim Đồng")
                        .param("diaChi", "Hà Nội")
                        .param("email", "info@nxbkimdong.com.vn")
                        .param("soDienThoai", "02439434730"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/publishers"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeletePublisher() throws Exception {
        when(sanPhamRepository.countByNhaXuatBan_MaNXB(1)).thenReturn(0L);

        mockMvc.perform(post("/admin/publishers/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/publishers"));
    }

    @Test
    @WithMockUser(username = "khachhang", roles = {"KHACHHANG"})
    void testCustomerCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?accessDenied=true"));
    }

    @Test
    void testAnonymousUserCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "quanly", roles = {"QUANLY"})
    void testManagerCanAccessAdminDashboard() throws Exception {
        when(sanPhamRepository.count()).thenReturn(100L);
        when(sanPhamRepository.countLowStock()).thenReturn(5L);
        when(sanPhamRepository.countOutOfStock()).thenReturn(2L);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }
}
