package boombimapi.domain.user.presentation.dto.res;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.user.domain.entity.User;

public record GetUserRes(
        String name,
        String profile,
        String email,
        SocialProvider socialProvider
) {
    public static GetUserRes of(User user) {
        return new GetUserRes(
                user.getName(),
                user.getProfile(),
                user.getEmail(),
                user.getSocialProvider()
        );
    }
}