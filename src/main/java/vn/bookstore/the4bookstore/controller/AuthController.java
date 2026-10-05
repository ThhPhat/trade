package vn.bookstore.the4bookstore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.bookstore.the4bookstore.dto.RegisterRequest;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.service.PasswordResetService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    private final vn.bookstore.the4bookstore.security.JwtService jwtService;

    public AuthController(TaiKhoanRepository taiKhoanRepository,
                          KhachHangRepository khachHangRepository,
                          PasswordEncoder passwordEncoder,
                          PasswordResetService passwordResetService,
                          vn.bookstore.the4bookstore.security.JwtService jwtService) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "adminDenied", required = false) String adminDenied,
                            @RequestParam(value = "switch", required = false) String switchAccount,
                            jakarta.servlet.http.HttpServletRequest request,
                            jakarta.servlet.http.HttpServletResponse response,
                            Authentication authentication) {
        if (adminDenied != null || switchAccount != null) {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            org.springframework.http.ResponseCookie deleteCookie = jwtService.cleanJwtCookie();
            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, deleteCookie.toString());
            return "auth/login";
        }

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("registerRequest") RegisterRequest request, Model model) {
        if (taiKhoanRepository.findByTenDangNhap(request.getTenDangNhap()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "auth/register";
        }
        if (taiKhoanRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "auth/register";
        }
        String phone = (request.getSoDienThoai() != null && !request.getSoDienThoai().isBlank()) ? request.getSoDienThoai().trim() : null;
        String address = (request.getDiaChi() != null && !request.getDiaChi().isBlank()) ? request.getDiaChi().trim() : null;

        if (phone != null && khachHangRepository.findBySoDienThoai(phone).isPresent()) {
            model.addAttribute("error", "Số điện thoại đã được sử dụng!");
            return "auth/register";
        }

        // Tạo tài khoản
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(request.getTenDangNhap());
        tk.setEmail(request.getEmail());
        tk.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        tk.setVaiTro("KHACHHANG");
        tk.setTrangThai("HoatDong");
        tk.setAuthProvider("LOCAL");
        taiKhoanRepository.save(tk);

        // Tạo hồ sơ khách hàng
        KhachHang kh = new KhachHang();
        kh.setHoTen(request.getHoTen());
        kh.setSoDienThoai(phone);
        kh.setEmail(request.getEmail());
        kh.setDiaChi(address);
        kh.setTaiKhoan(tk);
        khachHangRepository.save(kh);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/";
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        PasswordResetService.ResetResult result = passwordResetService.createPasswordResetOtp(email);

        if (result.status() == PasswordResetService.ResetStatus.SUCCESS) {
            redirectAttributes.addFlashAttribute("success", result.message());
            return "redirect:/reset-password?email=" + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
        } else if (result.status() == PasswordResetService.ResetStatus.GOOGLE_ACCOUNT) {
            model.addAttribute("isGoogleAccount", true);
            model.addAttribute("info", result.message());
            model.addAttribute("email", email);
            return "auth/forgot-password";
        } else {
            model.addAttribute("error", result.message());
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "email", required = false) String email,
                                    Authentication authentication,
                                    Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/";
        }

        if (email == null || email.trim().isEmpty()) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email.trim());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("email") String email,
                                       @RequestParam("otp") String otp,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        model.addAttribute("email", email);

        if (otp == null || otp.trim().length() != 6) {
            model.addAttribute("error", "Mã OTP phải gồm 6 chữ số!");
            return "auth/reset-password";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Mật khẩu mới phải có tối thiểu 6 ký tự!");
            return "auth/reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Xác nhận mật khẩu không trùng khớp!");
            return "auth/reset-password";
        }

        PasswordResetService.VerifyResult result = passwordResetService.verifyOtpAndResetPassword(email, otp, password);
        if (result.success()) {
            return "redirect:/login?resetSuccess=true";
        } else {
            model.addAttribute("error", result.message());
            return "auth/reset-password";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email,
                            RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/forgot-password";
        }

        PasswordResetService.ResetResult result = passwordResetService.createPasswordResetOtp(email);
        if (result.status() == PasswordResetService.ResetStatus.SUCCESS) {
            redirectAttributes.addFlashAttribute("success", "Mã xác thực OTP mới đã được gửi đến email của bạn!");
        } else {
            redirectAttributes.addFlashAttribute("error", result.message());
        }

        return "redirect:/reset-password?email=" + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
    }
}
