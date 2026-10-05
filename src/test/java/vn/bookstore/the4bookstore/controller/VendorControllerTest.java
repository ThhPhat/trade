package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.bookstore.the4bookstore.entity.Shop;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.*;
import vn.bookstore.the4bookstore.security.*;
import vn.bookstore.the4bookstore.service.ShopService;
import vn.bookstore.the4bookstore.service.VendorService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {VendorController.class, GlobalControllerAdvice.class})
@Import(SecurityConfig.class)
class VendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ShopService shopService;
    @MockitoBean private VendorService vendorService;
    @MockitoBean private TaiKhoanRepository taiKhoanRepository;
    @MockitoBean private DanhMucRepository danhMucRepository;
    @MockitoBean private NhaXuatBanRepository nhaXuatBanRepository;

    @MockitoBean private SanPhamRepository sanPhamRepository;
    @MockitoBean private KhachHangRepository khachHangRepository;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    @MockitoBean private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private CustomOidcUserService customOidcUserService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private TaiKhoan mockUser;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        mockUser = new TaiKhoan();
        mockUser.setMaTaiKhoan(10);
        mockUser.setTenDangNhap("customer");
        mockUser.setEmail("customer@gmail.com");
        mockUser.setVaiTro("KHACHHANG");
        mockUser.setTrangThai("HoatDong");

        when(taiKhoanRepository.findByTenDangNhap("customer")).thenReturn(Optional.of(mockUser));
        when(taiKhoanRepository.findByEmail("customer")).thenReturn(Optional.of(mockUser));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"KHACHHANG"})
    @DisplayName("Khách hàng truy cập GET /vendor/register thành công, hiển thị form đăng ký")
    void testRegisterForm_Success() throws Exception {
        when(shopService.findByTaiKhoan(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/vendor/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/register"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"KHACHHANG"})
    @DisplayName("Khách hàng đã có shop truy cập GET /vendor/register sẽ chuyển hướng về dashboard")
    void testRegisterForm_AlreadyHasShop_RedirectsToDashboard() throws Exception {
        Shop existingShop = new Shop();
        existingShop.setMaShop(1);
        when(shopService.findByTaiKhoan(any())).thenReturn(Optional.of(existingShop));

        mockMvc.perform(get("/vendor/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor/dashboard"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"KHACHHANG"})
    @DisplayName("Khách hàng gửi POST /vendor/register thành công mở gian hàng và redirect về dashboard")
    void testHandleRegister_Success() throws Exception {
        Shop newShop = new Shop();
        newShop.setMaShop(2);
        newShop.setTenShop("Nhà Sách Mới");
        when(shopService.registerShop(any(), anyString(), any(), anyString(), anyString(), anyString())).thenReturn(newShop);

        mockMvc.perform(post("/vendor/register")
                        .with(csrf())
                        .param("tenShop", "Nhà Sách Tri Thức Mới")
                        .param("moTa", "Chuyên các dòng sách kỹ năng sống")
                        .param("diaChiShop", "456 Lê Lợi, Q.1, TP.HCM")
                        .param("soDienThoai", "0908765432")
                        .param("emailShop", "trithucmoi@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(shopService, times(1)).registerShop(any(), eq("Nhà Sách Tri Thức Mới"), anyString(), eq("456 Lê Lợi, Q.1, TP.HCM"), eq("0908765432"), eq("trithucmoi@gmail.com"));
    }
}
