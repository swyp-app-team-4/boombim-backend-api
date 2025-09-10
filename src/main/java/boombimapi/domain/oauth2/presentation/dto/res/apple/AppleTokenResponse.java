package boombimapi.domain.oauth2.presentation.dto.res.apple;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("error")
        String error,

        @JsonProperty("error_description")
        String errorDescription
) {
    // Apple의 경우 사용자 정보는 ID Token에 있으므로 ID Token을 반환하는 메서드 추가
    public String getUserInfoToken() {
        return idToken != null ? idToken : accessToken;
    }

    // 에러 체크 메서드
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    // 에러 메시지 반환
    public String getErrorMessage() {
        if (hasError()) {
            return error + (errorDescription != null ? ": " + errorDescription : "");
        }
        return null;
    }
}