package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.security.*;
import vn.bookstore.the4bookstore.service.PasswordResetService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, GlobalControllerAdvice.class})
@Import(SecurityConfig.class)
class ResetPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaiKhoanRepository taiKhoanRepository;

    @MockitoBean
    private KhachHangRepository khachHangRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    @MockitoBean
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.when(jwtService.cleanJwtCookie())
                .thenReturn(org.springframework.http.ResponseCookie.from("the4book_jwt", "").path("/").maxAge(0).build());

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testGetResetPassword() throws Exception {
        mockMvc.perform(get("/reset-password").param("email", "caotuong14@gmail.com").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testGetResetPasswordWithFlashAttrs() throws Exception {
        mockMvc.perform(get("/reset-password")
                .param("email", "caotuong14@gmail.com")
                .flashAttr("success", "Mã OTP xác thực đặt lại mật khẩu đã được gửi đến email của bạn.")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostForgotPasswordRedirectsToResetPassword() throws Exception {
        org.mockito.Mockito.when(passwordResetService.createPasswordResetOtp("caotuong14@gmail.com"))
                .thenReturn(new vn.bookstore.the4bookstore.service.PasswordResetService.ResetResult(
                        vn.bookstore.the4bookstore.service.PasswordResetService.ResetStatus.SUCCESS,
                        "Mã OTP đã được gửi!",
                        null));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/forgot-password")
                .param("email", "caotuong14@gmail.com")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/reset-password?email=caotuong14%40gmail.com"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash().attribute("success", "Mã OTP đã được gửi!"));
    }

    @Test
    void testPostForgotPassword_GoogleAccount() throws Exception {
        org.mockito.Mockito.when(passwordResetService.createPasswordResetOtp("googleuser@gmail.com"))
                .thenReturn(new vn.bookstore.the4bookstore.service.PasswordResetService.ResetResult(
                        vn.bookstore.the4bookstore.service.PasswordResetService.ResetStatus.GOOGLE_ACCOUNT,
                        "Tài khoản của bạn được liên kết và đăng nhập bằng Google. Vui lòng sử dụng tính năng 'Đăng nhập bằng Google' để tiếp tục.",
                        null));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/forgot-password")
                .param("email", "googleuser@gmail.com")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("auth/forgot-password"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attribute("isGoogleAccount", true))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("info"));
    }

    @Test
    void testPostResetPasswordSuccess() throws Exception {
        org.mockito.Mockito.when(passwordResetService.verifyOtpAndResetPassword("caotuong14@gmail.com", "654321", "Password123@"))
                .thenReturn(new vn.bookstore.the4bookstore.service.PasswordResetService.VerifyResult(
                        true,
                        "Đặt lại mật khẩu thành công!"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/reset-password")
                .param("email", "caotuong14@gmail.com")
                .param("otp", "654321")
                .param("password", "Password123@")
                .param("confirmPassword", "Password123@")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/login?resetSuccess=true"));
    }

    @Test
    void testPostResendOtp() throws Exception {
        org.mockito.Mockito.when(passwordResetService.createPasswordResetOtp("caotuong14@gmail.com"))
                .thenReturn(new vn.bookstore.the4bookstore.service.PasswordResetService.ResetResult(
                        vn.bookstore.the4bookstore.service.PasswordResetService.ResetStatus.SUCCESS,
                        "Mã OTP đã được gửi!",
                        null));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/resend-otp")
                .param("email", "caotuong14@gmail.com")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/reset-password?email=caotuong14%40gmail.com"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash().attribute("success", "Mã xác thực OTP mới đã được gửi đến email của bạn!"));
    }
}
