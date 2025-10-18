package boombimapi.domain.oauth2.cookie.vo;

import org.springframework.http.ResponseCookie;

public record AuthCookies(
    ResponseCookie accessTokenCookie,
    ResponseCookie refreshTokenCookie
) {

    public static AuthCookies login(
        ResponseCookie accessTokenCookie,
        ResponseCookie refreshTokenCookie
    ) {
        return new AuthCookies(
            accessTokenCookie,
            refreshTokenCookie
        );
    }

    public static AuthCookies reissue(
        ResponseCookie accessTokenCookie,
        ResponseCookie refreshTokenCookie
    ) {
        return new AuthCookies(
            accessTokenCookie,
            refreshTokenCookie
        );
    }

    public static AuthCookies logout(
        ResponseCookie logoutAccessTokenCookie,
        ResponseCookie logoutRefreshTokenCookie
    ) {
        return new AuthCookies(
            logoutAccessTokenCookie,
            logoutRefreshTokenCookie
        );
    }

}
