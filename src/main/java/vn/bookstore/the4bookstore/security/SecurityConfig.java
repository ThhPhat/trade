package vn.bookstore.the4bookstore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          CustomOidcUserService customOidcUserService,
                          OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtLogoutSuccessHandler jwtLogoutSuccessHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtLogoutSuccessHandler = jwtLogoutSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/kho", "/admin/kho/**", "/kho/**").hasAnyRole("ADMIN", "QUANLY", "NHANVIENKHO")
                .requestMatchers("/admin/orders", "/admin/orders/**").hasAnyRole("ADMIN", "QUANLY", "NHANVIENBANHANG")
                .requestMatchers("/admin", "/admin/**").hasAnyRole("ADMIN", "QUANLY")
                .requestMatchers("/ban-hang/**").hasAnyRole("ADMIN", "QUANLY", "NHANVIENBANHANG")
                .requestMatchers("/khach-hang/**", "/thanh-toan/**").hasAnyRole("KHACHHANG", "ADMIN", "QUANLY")
                .requestMatchers("/profile", "/profile/**").authenticated()
                .requestMatchers("/don-hang", "/don-hang/**").authenticated()
                .requestMatchers("/gio-hang/dat-hang").authenticated()
                .requestMatchers("/api/gio-hang/**").permitAll()
                .requestMatchers("/gio-hang", "/gio-hang/**").permitAll()
                .requestMatchers("/forgot-password", "/forgot-password/**", "/reset-password", "/reset-password/**", "/resend-otp").permitAll()
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/admin") || request.getRequestURI().startsWith("/kho")) {
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
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oauth2LoginSuccessHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(jwtLogoutSuccessHandler)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}