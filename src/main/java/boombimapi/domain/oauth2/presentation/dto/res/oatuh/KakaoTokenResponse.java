package boombimapi.domain.oauth2.presentation.dto.res.oatuh;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("expires_in")
        Long expiresIn
) {
}