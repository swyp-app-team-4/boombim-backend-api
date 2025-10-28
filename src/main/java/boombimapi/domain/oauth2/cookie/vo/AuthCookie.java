package boombimapi.domain.oauth2.cookie.vo;

import org.springframework.http.ResponseCookie;

public record AuthCookie(
    ResponseCookie refreshTokenCookie
) {
    public static AuthCookie createRefreshTokenCookie(
        ResponseCookie refreshTokenCookie
    ) {
        return new AuthCookie(
            refreshTokenCookie
        );
    }


}
