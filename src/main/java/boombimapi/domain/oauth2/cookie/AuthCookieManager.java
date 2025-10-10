package boombimapi.domain.oauth2.cookie;

import boombimapi.global.properties.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieManager {

    private final CookieProperties cookieProperties;

    public void addTokens(
        HttpServletResponse response,
        String accessToken,
        String refreshToken
    ) {
        addAccessToken(response, accessToken);
        addRefreshToken(response, refreshToken);
    }

    public String extractRefreshToken(
        HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        return Arrays.stream(cookies)
            .filter(cookie -> cookieProperties.rtName().equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }

    private void addAccessToken(
        HttpServletResponse response,
        String accessToken
    ) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.atName(), accessToken)
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite(cookieProperties.sameSite())
            .path("/")
            .maxAge(cookieProperties.atMaxAgeMillis() / 1000)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void addRefreshToken(
        HttpServletResponse response,
        String refreshToken
    ) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.rtName(), refreshToken)
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite(cookieProperties.sameSite())
            .path("/")
            .maxAge(cookieProperties.rtMaxAgeMillis() / 1000)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

}
