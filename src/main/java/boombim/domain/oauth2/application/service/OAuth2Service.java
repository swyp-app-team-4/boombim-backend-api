package boombim.domain.oauth2.application.service;

import boombim.domain.oauth2.domain.entity.SocialProvider;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;

public interface OAuth2Service {
    String getLoginUrl();
    OAuth2TokenResponse getTokens(String code);
    OAuth2TokenResponse refreshTokens(String refreshToken);
    OAuth2UserResponse getUserInfo(String accessToken);
    SocialProvider getProvider();
}