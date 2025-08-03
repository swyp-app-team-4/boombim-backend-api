package boombimapi.domain.oauth2.presentation.dto.response.oatuh;

import com.fasterxml.jackson.annotation.JsonProperty;

// 수정된 OAuth2UserResponse 클래스
public record OAuth2UserResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
        public String getName() {
                return kakaoAccount != null && kakaoAccount.profile != null
                        ? kakaoAccount.profile.nickname : null;
        }

        public String getEmail() {
                return kakaoAccount != null ? kakaoAccount.email : null;
        }

        public String getProfile() {
                return kakaoAccount != null && kakaoAccount.profile != null
                        ? kakaoAccount.profile.profileImageUrl : null;
        }

        public record KakaoAccount(
                @JsonProperty("profile")
                Profile profile,

                @JsonProperty("email")
                String email
        ) {}

        public record Profile(
                @JsonProperty("nickname")
                String nickname,

                @JsonProperty("profile_image_url")
                String profileImageUrl
        ) {}
}
