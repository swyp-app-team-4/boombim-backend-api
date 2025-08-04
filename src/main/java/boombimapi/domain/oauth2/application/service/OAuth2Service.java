package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoUserResponse;

public interface OAuth2Service {
    String getLoginUrl();
    KakaoTokenResponse getTokens(String code);
    KakaoTokenResponse refreshTokens(String refreshToken);
    KakaoUserResponse getUserInfo(String accessToken);
    SocialProvider getProvider();
}