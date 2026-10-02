package vn.bookstore.the4bookstore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.KhachHangRepository;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;
import vn.bookstore.the4bookstore.service.GioHangService;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final JwtService jwtService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final GioHangService gioHangService;

    public OAuth2LoginSuccessHandler(JwtService jwtService,
                                     TaiKhoanRepository taiKhoanRepository,
                                     KhachHangRepository khachHangRepository,
                                     GioHangService gioHangService) {
        this.jwtService = jwtService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.gioHangService = gioHangService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        // 1. Tìm thông tin tài khoản và họ tên
        TaiKhoan tk = resolveTaiKhoan(authentication);
        String displayName = resolveDisplayName(authentication, tk);

        // Merge giỏ hàng session vào DB nếu có
        if (tk != null && gioHangService != null) {
            khachHangRepository.findByTaiKhoan(tk).ifPresent(kh -> {
                gioHangService.mergeSessionCartToDb(kh, request.getSession(false));
            });
        }

        // 2. Phát hành JWT Token và ghi vào HttpOnly Cookie
        String token = jwtService.generateToken(authentication, tk, displayName);
        ResponseCookie jwtCookie = jwtService.createJwtCookie(token);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        // 3. Phân luồng điều hướng theo vai trò
        boolean isAdminOrManager = authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_QUANLY".equals(a.getAuthority())
        );
        boolean isWarehouse = authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_NHANVIENKHO".equals(a.getAuthority())
        );
        boolean isSales = authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_NHANVIENBANHANG".equals(a.getAuthority())
        );

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (isAdminOrManager) {
            // Nếu là Admin/Quản lý: ưu tiên vào route admin nếu có yêu cầu trước đó, ngược lại vào /admin/dashboard
            if (savedRequest != null && savedRequest.getRedirectUrl().contains("/admin")) {
                getRedirectStrategy().sendRedirect(request, response, savedRequest.getRedirectUrl());
                return;
            }
            getRedirectStrategy().sendRedirect(request, response, "/admin/dashboard");
            return;
        }

        if (isWarehouse) {
            if (savedRequest != null && (savedRequest.getRedirectUrl().contains("/kho") || savedRequest.getRedirectUrl().contains("/admin/kho"))) {
                getRedirectStrategy().sendRedirect(request, response, savedRequest.getRedirectUrl());
                return;
            }
            getRedirectStrategy().sendRedirect(request, response, "/admin/kho");
            return;
        }

        if (isSales) {
            if (savedRequest != null && savedRequest.getRedirectUrl().contains("/orders")) {
                getRedirectStrategy().sendRedirect(request, response, savedRequest.getRedirectUrl());
                return;
            }
            getRedirectStrategy().sendRedirect(request, response, "/admin/orders");
            return;
        }

        // Người dùng là Khách hàng (ROLE_KHACHHANG):
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            // Nếu trước đó có savedRequest trỏ tới admin/kho/ban-hang hoặc luồng oauth/login/error:
            if (targetUrl.contains("/admin") || targetUrl.contains("/kho") || targetUrl.contains("/ban-hang")
                    || targetUrl.contains("/login") || targetUrl.contains("/oauth2") || targetUrl.contains("/error")) {
                requestCache.removeRequest(request, response);
                getRedirectStrategy().sendRedirect(request, response, "/");
                return;
            }
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // Mặc định khách hàng quay về trang chủ
        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    private TaiKhoan resolveTaiKhoan(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getTaiKhoan();
        }
        if (principal instanceof CustomOAuth2User oAuth2User) {
            if (oAuth2User.getTaiKhoan() != null) return oAuth2User.getTaiKhoan();
            if (oAuth2User.getEmail() != null) {
                return taiKhoanRepository.findByEmail(oAuth2User.getEmail()).orElse(null);
            }
        }
        if (principal instanceof CustomOidcUser oidcUser) {
            if (oidcUser.getTaiKhoan() != null) return oidcUser.getTaiKhoan();
            if (oidcUser.getEmail() != null) {
                return taiKhoanRepository.findByEmail(oidcUser.getEmail()).orElse(null);
            }
        }

        String name = authentication.getName();
        return taiKhoanRepository.findByTenDangNhap(name)
                .or(() -> taiKhoanRepository.findByEmail(name))
                .orElse(null);
    }

    private String resolveDisplayName(Authentication authentication, TaiKhoan tk) {
        if (tk != null) {
            Optional<KhachHang> khOpt = khachHangRepository.findByTaiKhoan(tk);
            if (khOpt.isPresent() && khOpt.get().getHoTen() != null && !khOpt.get().getHoTen().isBlank() && !khOpt.get().getHoTen().matches("\\d+")) {
                return khOpt.get().getHoTen();
            }
            if (tk.getTenDangNhap() != null && !tk.getTenDangNhap().matches("\\d+")) {
                return tk.getTenDangNhap();
            }
        }
        String name = authentication.getName();
        return (name != null && !name.matches("\\d+")) ? name : "User";
    }
}
