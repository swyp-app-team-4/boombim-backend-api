package boombim.domain.user.presentation.dto.res;

import boombim.domain.oauth2.domain.entity.SocialProvider;
import boombim.domain.user.domain.entity.User;

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