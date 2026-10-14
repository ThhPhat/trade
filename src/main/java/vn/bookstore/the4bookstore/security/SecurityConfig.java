package vn.bookstore.the4bookstore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          CustomOidcUserService customOidcUserService,
                          OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) CustomAuthenticationFailureHandler customAuthenticationFailureHandler,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtLogoutSuccessHandler jwtLogoutSuccessHandler,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) RateLimitFilter rateLimitFilter) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtLogoutSuccessHandler = jwtLogoutSuccessHandler;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/api/webhooks/**", "/don-hang/api/**", "/ws/**"))
            .authorizeHttpRequests(auth -> auth
                // Phân quyền cho Vendor Portal
                .requestMatchers("/vendor", "/vendor/register", "/vendor/register/**", "/vendor/pending", "/vendor/blocked").authenticated()
                .requestMatchers("/vendor/**").hasAnyRole("VENDOR", "ADMIN", "MANAGER", "QUANLY")
                // Phân quyền cho Quản trị Admin & Manager (Manager duyệt gian hàng)
                .requestMatchers("/admin/shops", "/admin/shops/**", "/manager/shops", "/manager/shops/**").hasAnyRole("MANAGER", "QUANLY", "ADMIN")
                .requestMatchers("/admin/platform-fees", "/admin/platform-fees/**").hasAnyRole("ADMIN", "QUANLY", "MANAGER")
                .requestMatchers("/admin/system/**", "/admin/managers/**").hasRole("ADMIN")
                .requestMatchers("/admin/users", "/admin/users/**").hasAnyRole("ADMIN", "QUANLY", "MANAGER")
                .requestMatchers("/admin/kho", "/admin/kho/**", "/kho/**").hasAnyRole("ADMIN", "QUANLY", "MANAGER", "NHANVIENKHO")
                .requestMatchers("/admin/orders", "/admin/orders/**").hasAnyRole("ADMIN", "QUANLY", "MANAGER", "NHANVIENBANHANG")
                .requestMatchers("/admin", "/admin/**", "/manager", "/manager/**").hasAnyRole("ADMIN", "QUANLY", "MANAGER")
                .requestMatchers("/ban-hang/**").hasAnyRole("ADMIN", "QUANLY", "MANAGER", "NHANVIENBANHANG")
                .requestMatchers("/khach-hang/**").hasAnyRole("KHACHHANG", "USER", "ADMIN", "QUANLY", "MANAGER")
                // Webhook và API kiểm tra trạng thái thanh toán công khai
                .requestMatchers("/don-hang/api/vietqr-webhook", "/don-hang/api/webhooks/**", "/don-hang/api/*/trang-thai-thanh-toan").permitAll()
                // Yêu cầu xác thực tài khoản
                .requestMatchers("/profile", "/profile/**", "/dia-chi/**", "/api/dia-chi/**", "/api/favorites/**", "/api/danh-gia/**").authenticated()
                .requestMatchers("/don-hang", "/don-hang/**", "/gio-hang/dat-hang", "/thanh-toan/**").authenticated()
                // Công khai (Public routes)
                .requestMatchers("/api/gio-hang/**").permitAll()
                .requestMatchers("/gio-hang", "/gio-hang/**").permitAll()
                .requestMatchers("/shop", "/shop/**").permitAll()
                .requestMatchers("/san-voucher", "/san-voucher/**", "/vouchers/**", "/api/voucher/**").permitAll()
                .requestMatchers("/register", "/register/**", "/login", "/process-login", "/forgot-password", "/forgot-password/**", "/reset-password", "/reset-password/**", "/verify-otp", "/resend-otp").permitAll()
                .requestMatchers("/", "/home", "/san-pham/**", "/products/**", "/search/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error", "/favicon.ico", "/favicon.png").permitAll()
                .requestMatchers("/api/webhooks/**").permitAll()
                .requestMatchers("/ws", "/ws/**").permitAll()
                // Fail-Close policy: Bất kỳ URL nào khác đều phải xác thực
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/admin") || request.getRequestURI().startsWith("/kho") || request.getRequestURI().startsWith("/vendor")) {
                        response.sendRedirect(request.getContextPath() + "/?accessDenied=true");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/");
                    }
                })
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                .successHandler(oauth2LoginSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler != null ? customAuthenticationFailureHandler : new org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler("/login?error=true"))
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oauth2LoginSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    org.slf4j.LoggerFactory.getLogger("OAuth2Security").error("Google OAuth2 login failed: {}", exception != null ? exception.getMessage() : "Unknown error", exception);
                    if (exception != null && exception.getMessage() != null && (exception.getMessage().contains("account_locked") || exception.getMessage().contains("khóa"))) {
                        response.sendRedirect(request.getContextPath() + "/login?locked=true");
                    } else {
                        String msg = (exception != null && exception.getMessage() != null) ? exception.getMessage() : "Đăng nhập Google thất bại";
                        String encodedMsg = java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8);
                        response.sendRedirect(request.getContextPath() + "/login?oauthError=" + encodedMsg);
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(jwtLogoutSuccessHandler)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .maximumSessions(3)
                .sessionRegistry(sessionRegistry())
            );

        if (rateLimitFilter != null) {
            http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        }
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}