package boombimapi.domain.oauth2.presentation.dto.response.naver;

import boombimapi.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserResponse(
        @JsonProperty("resultcode") String resultCode,
        @JsonProperty("message") String message,
        @JsonProperty("response") UserInfo response
) {
    public record UserInfo(
            @JsonProperty("id") String id,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("profile_image") String profileImage
    ) {}

    // OAuth2UserResponse로 변환하는 메서드
    public OAuth2UserResponse toOAuth2UserResponse() {
        if (response == null) return null;

        return new OAuth2UserResponse(
                response.id(),
                new OAuth2UserResponse.KakaoAccount(
                        new OAuth2UserResponse.Profile(
                                response.nickname(),
                                response.profileImage()
                        ),
                        response.email()
                )
        );
    }
}