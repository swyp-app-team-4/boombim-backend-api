package boombimapi.domain.member.presentation.dto.res;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.member.domain.entity.Member;
public record GetMemberRes(
        String name,
        String profile,
        String email,
        SocialProvider socialProvider
) {
    public static GetMemberRes of(Member user) {
        return new GetMemberRes(
                user.getName(),
                user.getProfile(),
                user.getEmail(),
                user.getSocialProvider()
        );
    }
}