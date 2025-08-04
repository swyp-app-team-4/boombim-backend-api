package boombimapi.domain.oauth2.application.service.impl;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import boombimapi.global.infra.feignclient.kakao.KakaoOAuth2URLFeignClient;
import boombimapi.global.infra.feignclient.kakao.KakaoOAuth2UserFeignClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service  // ← 이 어노테이션이 누락되었을 가능성이 높습니다!
@Transactional
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuth2ServiceImpl implements OAuth2Service {
    private final KakaoOAuth2URLFeignClient kakaoOAuth2URLFeignClient;
    private final KakaoOAuth2UserFeignClient kakaoOAuth2UserFeignClient;

    @Value("${oauth2.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth2.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth2.kakao.base-url}")
    private String baseUrl;

    @Override
    public String getLoginUrl() {
        log.info("카카오 로그인 URL 생성: clientId={}, redirectUri={}", clientId, redirectUri);
        return baseUrl +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=profile_nickname,profile_image,account_email";
    }

    @Override
    public OAuth2TokenResponse getTokens(String code) {
        return kakaoOAuth2URLFeignClient.getAccessToken(
                code, clientId, clientSecret, redirectUri, "authorization_code"
        );
    }

    @Override
    public OAuth2TokenResponse refreshTokens(String refreshToken) {
        return kakaoOAuth2URLFeignClient.refreshToken(
                "refresh_token", refreshToken, clientId, clientSecret
        );
    }

    @Override
    public OAuth2UserResponse getUserInfo(String accessToken) {
        return kakaoOAuth2UserFeignClient.getUserInfo("Bearer " + accessToken);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }
}