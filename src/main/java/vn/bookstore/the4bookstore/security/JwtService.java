package vn.bookstore.the4bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import vn.bookstore.the4bookstore.entity.TaiKhoan;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty!");
        }
    }

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.cookie-name:the4book_jwt}")
    private String cookieName;

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication, TaiKhoan taiKhoan, String displayName) {
        String username = (taiKhoan != null && taiKhoan.getTenDangNhap() != null)
                ? taiKhoan.getTenDangNhap()
                : authentication.getName();
        String email = (taiKhoan != null) ? taiKhoan.getEmail() : "";
        Integer userId = (taiKhoan != null) ? taiKhoan.getMaTaiKhoan() : null;

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("email", email)
                .claim("roles", roles)
                .claim("userId", userId)
                .claim("tokenVersion", taiKhoan != null ? taiKhoan.getTokenVersion() : 1)
                .claim("displayName", displayName != null ? displayName : username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        Object email = extractAllClaims(token).get("email");
        return email != null ? email.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    public Integer extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        if (userId instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    public Integer extractTokenVersion(String token) {
        Object ver = extractAllClaims(token).get("tokenVersion");
        if (ver instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    public String extractDisplayName(String token) {
        Object name = extractAllClaims(token).get("displayName");
        return name != null ? name.toString() : null;
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getCookieName() {
        return cookieName;
    }

    public int getCookieMaxAgeSeconds() {
        return (int) (jwtExpirationMs / 1000);
    }

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .path("/")
                .maxAge(getCookieMaxAgeSeconds())
                .httpOnly(true)
                .sameSite("Lax")
                .secure(cookieSecure)
                .build();
    }

    public ResponseCookie cleanJwtCookie() {
        return ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(cookieSecure)
                .build();
    }
}
