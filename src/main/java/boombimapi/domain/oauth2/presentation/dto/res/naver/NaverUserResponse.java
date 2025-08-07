package boombimapi.domain.oauth2.presentation.dto.res.naver;

import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;
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
    ) {
    }

    // OAuth2UserResponse로 변환하는 메서드
    public KakaoUserResponse toOAuth2UserResponse() {
        if (response == null) return null;

        return new KakaoUserResponse(
                response.id(),
                new KakaoUserResponse.KakaoAccount(
                        new KakaoUserResponse.Profile(
                                response.nickname(),
                                response.profileImage()
                        ),
                        response.email()
                )
        );
    }
}