package vn.bookstore.the4bookstore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.bookstore.the4bookstore.entity.TaiKhoan;
import vn.bookstore.the4bookstore.repository.TaiKhoanRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TaiKhoanRepository taiKhoanRepository;

    public JwtAuthenticationFilter(JwtService jwtService, TaiKhoanRepository taiKhoanRepository) {
        this.jwtService = jwtService;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            String email = jwtService.extractEmail(token);
            List<String> roles = jwtService.extractRoles(token);

            Optional<TaiKhoan> tkOpt = Optional.empty();
            if (username != null && !username.isBlank()) {
                tkOpt = taiKhoanRepository.findByTenDangNhap(username);
            }
            if (tkOpt.isEmpty() && email != null && !email.isBlank()) {
                tkOpt = taiKhoanRepository.findByEmail(email);
            }

            if (tkOpt.isPresent()) {
                TaiKhoan taiKhoan = tkOpt.get();
                if ("BiKhoa".equalsIgnoreCase(taiKhoan.getTrangThai())) {
                    org.springframework.http.ResponseCookie cleanCookie = jwtService.cleanJwtCookie();
                    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cleanCookie.toString());
                    SecurityContextHolder.clearContext();
                    if (request.getSession(false) != null) {
                        try {
                            request.getSession(false).invalidate();
                        } catch (Exception ignored) {}
                    }
                    filterChain.doFilter(request, response);
                    return;
                }

                Integer tokenVer = jwtService.extractTokenVersion(token);
                if (tokenVer != null && !tokenVer.equals(taiKhoan.getTokenVersion())) {
                    // Token version mismatch - token has been invalidated
                    org.springframework.http.ResponseCookie cleanCookie = jwtService.cleanJwtCookie();
                    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cleanCookie.toString());
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                CustomUserDetails userDetails = new CustomUserDetails(taiKhoan);

                org.springframework.security.core.Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                boolean shouldUpdate = (currentAuth == null 
                        || !currentAuth.isAuthenticated() 
                        || "anonymousUser".equals(currentAuth.getPrincipal())
                        || currentAuth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_" + taiKhoan.getVaiTro())));

                if (shouldUpdate) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    if (request.getSession(false) != null) {
                        request.getSession(false).setAttribute(
                                org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                SecurityContextHolder.getContext()
                        );
                    }
                }
            } else if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Ưu tiên lấy từ HttpOnly Cookie (dành cho các trang Thymeleaf SSR)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtService.getCookieName().equals(cookie.getName())) {
                    if (cookie.getValue() != null && !cookie.getValue().isBlank()) {
                        return cookie.getValue();
                    }
                }
            }
        }

        // 2. Dự phòng lấy từ Header Authorization Bearer (dành cho API / Mobile / SPA)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
