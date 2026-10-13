package vn.bookstore.the4bookstore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import vn.bookstore.the4bookstore.service.SecurityAuditService;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final SecurityAuditService securityAuditService;

    public CustomAuthenticationFailureHandler(@org.springframework.beans.factory.annotation.Autowired(required = false) SecurityAuditService securityAuditService) {
        super("/login?error=true");
        this.securityAuditService = securityAuditService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String ip = getClientIp(request);

        if (exception instanceof LockedException || exception instanceof DisabledException
                || (exception.getCause() instanceof LockedException)
                || (exception.getCause() instanceof DisabledException)
                || (exception.getMessage() != null && (exception.getMessage().contains("locked") || exception.getMessage().contains("disabled") || exception.getMessage().contains("khóa")))) {
            if (securityAuditService != null) {
                securityAuditService.logLoginFailure(username, ip, "Tài khoản bị khóa hoặc vô hiệu hóa");
            }
            getRedirectStrategy().sendRedirect(request, response, "/login?locked=true");
        } else {
            if (securityAuditService != null) {
                securityAuditService.logLoginFailure(username, ip, "Sai thông tin đăng nhập");
            }
            getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
