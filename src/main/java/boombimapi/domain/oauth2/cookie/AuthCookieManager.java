package boombimapi.domain.oauth2.cookie;

import static boombimapi.domain.oauth2.cookie.type.AuthCookieType.*;

import boombimapi.domain.oauth2.cookie.type.AuthCookieType;
import boombimapi.domain.oauth2.cookie.vo.AuthCookies;
import boombimapi.global.properties.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieManager {

    private final CookieProperties cookieProperties;

    public AuthCookies createAuthCookies(
        String accessToken,
        String refreshToken
    ) {
        return AuthCookies.login(
            buildCookie(ACCESS, accessToken),
            buildCookie(REFRESH, refreshToken)
        );
    }

    public AuthCookies reissueAuthCookies(
        String newAccessToken,
        String newRefreshToken
    ) {
        return AuthCookies.reissue(
            buildCookie(ACCESS, newAccessToken),
            buildCookie(REFRESH, newRefreshToken)
        );
    }

    public AuthCookies logout() {
        return AuthCookies.logout(
            buildLogoutCookie(ACCESS),
            buildLogoutCookie(REFRESH)
        );
    }

    public String extractRefreshToken(
        HttpServletRequest request
    ) {
        if (request == null) {
            return null;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieProperties.rtName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie buildCookie(
        AuthCookieType authCookieType,
        String value
    ) {
        long maxAgeMillis = authCookieType.maxAgeFrom(cookieProperties);

        return ResponseCookie
            .from(authCookieType.nameFrom(cookieProperties), value)
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite(cookieProperties.sameSite())
            .path(cookieProperties.path())
            .maxAge(Duration.ofSeconds(Math.max(0, maxAgeMillis / 1000)))
            .build();
    }

    private ResponseCookie buildLogoutCookie(
        AuthCookieType authCookieType
    ) {
        return ResponseCookie
            .from(authCookieType.nameFrom(cookieProperties), "")
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite(cookieProperties.sameSite())
            .path(cookieProperties.path())
            .maxAge(Duration.ZERO)
            .build();
    }
}
