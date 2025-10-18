package boombimapi.domain.oauth2.cookie.vo;

import org.springframework.http.ResponseCookie;

public record AuthCookies(
    ResponseCookie accessToken,
    ResponseCookie refreshToken
) {

    public static AuthCookies of(
        ResponseCookie accessToken,
        ResponseCookie refreshToken
    ) {
        return new AuthCookies(
            accessToken,
            refreshToken
        );
    }

}
