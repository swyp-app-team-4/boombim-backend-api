package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.KakaoAccessTokenAndRefreshTokenService;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombim.global.infra.feignclient.kakao.KakaoOAuth2URLFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAccessTokenAndRefreshTokenServiceImpl implements KakaoAccessTokenAndRefreshTokenService {

    private final KakaoOAuth2URLFeignClient KakaoOAuth2URLFeignClient;

    @Value("${oauth2.client-id}")
    private String clientId;

    @Value("${oauth2.client-secret}")
    private String clientSecret;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuth2TokenResponse getAccessTokenAndRefreshToken(String code) {
        return KakaoOAuth2URLFeignClient.getAccessToken(
                code,
                clientId,
                clientSecret,
                redirectUri,
                "authorization_code"
        );
    }

    @Override
    public OAuth2TokenResponse refreshAccessToken(String refreshToken) {
        // refresh token을 사용해 새로운 access token을 발급받는 로직
        return KakaoOAuth2URLFeignClient.refreshToken(
                "refresh_token",
                refreshToken,
                clientId,
                clientSecret
        );
    }
}
