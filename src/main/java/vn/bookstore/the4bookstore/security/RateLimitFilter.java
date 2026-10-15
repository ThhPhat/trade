package vn.bookstore.the4bookstore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private static class RateLimitEntry {
        AtomicInteger count = new AtomicInteger(1);
        long firstRequestTime = System.currentTimeMillis();
    }

    private final Map<String, RateLimitEntry> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, RateLimitEntry> otpAttempts = new ConcurrentHashMap<>();

    private static final long LOGIN_TIME_WINDOW = 15 * 60 * 1000; // 15 minutes
    private static final int LOGIN_MAX_REQUESTS = 10;

    private static final long OTP_TIME_WINDOW = 5 * 60 * 1000; // 5 minutes
    private static final int OTP_MAX_REQUESTS = 15;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            String ipAddress = getClientIp(request);
            if ("/process-login".equals(path)) {
                if (!isAllowed(loginAttempts, ipAddress, LOGIN_TIME_WINDOW, LOGIN_MAX_REQUESTS)) {
                    sendRateLimitError(request, response, "Quá nhiều lần thử đăng nhập. Vui lòng thử lại sau.", "/login?error=true");
                    return;
                }
            } else if ("/forgot-password".equals(path) || "/resend-otp".equals(path) || "/reset-password".equals(path)) {
                if (!isAllowed(otpAttempts, ipAddress, OTP_TIME_WINDOW, OTP_MAX_REQUESTS)) {
                    String redirectUrl = "/forgot-password?rateLimited=true";
                    if ("/reset-password".equals(path) || "/resend-otp".equals(path)) {
                        String email = request.getParameter("email");
                        if (email != null && !email.isBlank()) {
                            redirectUrl = "/reset-password?email=" + java.net.URLEncoder.encode(email.trim(), java.nio.charset.StandardCharsets.UTF_8) + "&rateLimited=true";
                        }
                    }
                    sendRateLimitError(request, response, "Quá nhiều yêu cầu OTP. Vui lòng thử lại sau 5 phút.", redirectUrl);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(Map<String, RateLimitEntry> cache, String key, long timeWindow, int maxRequests) {
        long currentTime = System.currentTimeMillis();
        RateLimitEntry entry = cache.compute(key, (k, v) -> {
            if (v == null || (currentTime - v.firstRequestTime) > timeWindow) {
                return new RateLimitEntry();
            }
            v.count.incrementAndGet();
            return v;
        });

        if (entry.count.get() > maxRequests) {
            logger.warn("Rate limit exceeded for IP: {}", key);
            return false;
        }
        return true;
    }

    private void sendRateLimitError(HttpServletRequest request, HttpServletResponse response, String message, String formRedirectUrl) throws IOException {
        String accept = request.getHeader("Accept");
        boolean isApiOrJson = (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE))
                || "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        if (!isApiOrJson && formRedirectUrl != null) {
            response.sendRedirect(request.getContextPath() + formRedirectUrl);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String safeMsg = message != null ? message.replace("\"", "\\\"") : "";
        String json = "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + safeMsg + "\"}";
        response.getWriter().write(json);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // Run every 30 minutes
    public void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        loginAttempts.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue().firstRequestTime) > LOGIN_TIME_WINDOW);
            
        otpAttempts.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue().firstRequestTime) > OTP_TIME_WINDOW);
            
        logger.info("Cleaned up expired rate limit entries");
    }
}
