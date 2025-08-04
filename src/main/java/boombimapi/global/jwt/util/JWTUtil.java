package boombimapi.global.jwt.util;

import boombimapi.domain.user.domain.entity.Role;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTUtil {

    private final SecretKey secretKey;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public JWTUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getId(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", String.class);
        }
        catch(ExpiredJwtException e) {
            throw new BoombimException(ErrorCode.JWT_EXPIRE_TOKEN);
        }
        catch(JwtException e) {
            throw new BoombimException(ErrorCode.JWT_ERROR_TOKEN);
        }
    }

    public Role getRole(String token) {
        try {
            return Role.getByValue("ROLE_" + Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class));
        }
        catch(ExpiredJwtException e) {
            throw new BoombimException(ErrorCode.JWT_EXPIRE_TOKEN);
        }
        catch(JwtException e) {
            throw new BoombimException(ErrorCode.JWT_ERROR_TOKEN);
        }
    }

    public String createAccessToken(String id, Role role, String email) {
        return createJWT(id, role, email, "access", accessExpiration);
    }

    public String createRefreshToken(String id, Role role, String email) {
        return createJWT(id, role, email, "refresh", refreshExpiration);
    }

    private String createJWT(String id, Role role, String email, String category, Long expiredMS) {
        return Jwts.builder()
                .claim("category", category)
                .claim("id", id)
                .claim("role", String.valueOf(role))
                .claim("email", email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMS))
                .signWith(secretKey)
                .compact();
    }

    public String getAccessTokenFromHeaders(HttpServletRequest request) {
        if(request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            return request.getHeader("Authorization").replace("Bearer ", "");
        }
        return null;
    }

    public boolean jwtVerify(String token, String type) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            String tokenType = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
            return tokenType != null && tokenType.equals(type);
        }
        catch(JwtException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}