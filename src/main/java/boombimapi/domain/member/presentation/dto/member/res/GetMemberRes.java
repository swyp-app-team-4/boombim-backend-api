package boombimapi.domain.member.presentation.dto.member.res;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.member.domain.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 1번 구간 응답")
public record GetMemberRes(

        @Schema(description = "사용자 이름", example = "최승호")
        String name,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
        String profile,

        @Schema(description = "사용자 이메일", example = "test@example.com")
        String email,

        @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
        SocialProvider socialProvider,

        @Schema(description = "사용자 포인트", example = "12")
        Long Point

) {
    public static GetMemberRes of(Member user, Long point) {
        return new GetMemberRes(
                user.getName(),
                user.getProfile(),
                user.getEmail(),
                user.getSocialProvider(),
                point

        );
    }
}
