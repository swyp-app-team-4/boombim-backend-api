package boombimapi.domain.oauth2.presentation.dto.response;

public record LoginToken(
        String accessToken,
        String refreshToken
) {
    public static LoginToken of(String accessToken, String refreshToken) {
        return new LoginToken(accessToken, refreshToken);
    }
}
