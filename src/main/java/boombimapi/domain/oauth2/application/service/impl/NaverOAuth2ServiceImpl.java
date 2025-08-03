package boombimapi.domain.oauth2.application.service.impl;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.response.naver.NaverTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.naver.NaverUserResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import boombimapi.global.infra.feignclient.naver.NaverOAuth2URLFeignClient;
import boombimapi.global.infra.feignclient.naver.NaverOAuth2UserFeignClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NaverOAuth2ServiceImpl implements OAuth2Service {
    private final NaverOAuth2URLFeignClient naverOAuth2URLFeignClient;
    private final NaverOAuth2UserFeignClient naverOAuth2UserFeignClient;

    @Value("${oauth2.naver.client-id}")
    private String clientId;

    @Value("${oauth2.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth2.naver.redirect-uri}")
    private String redirectUri;

    @Value("${oauth2.naver.base-url}")
    private String baseUrl;

    @Override
    public String getLoginUrl() {
        String state = generateState(); // UUID 등을 이용한 state 생성
        return baseUrl +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + state;
    }

    @Override
    public OAuth2TokenResponse getTokens(String code) {
        NaverTokenResponse naverResponse = naverOAuth2URLFeignClient.getAccessToken(
                "authorization_code", clientId, clientSecret, redirectUri, code, "state_value"
        );

        return new OAuth2TokenResponse(
                naverResponse.accessToken(),
                naverResponse.refreshToken(),
                naverResponse.expiresIn()
        );
    }

    @Override
    public OAuth2TokenResponse refreshTokens(String refreshToken) {
        NaverTokenResponse naverResponse = naverOAuth2URLFeignClient.refreshToken(
                "refresh_token", clientId, clientSecret, refreshToken
        );

        return new OAuth2TokenResponse(
                naverResponse.accessToken(),
                naverResponse.refreshToken(),
                naverResponse.expiresIn()
        );
    }

    @Override
    public OAuth2UserResponse getUserInfo(String accessToken) {
        NaverUserResponse naverUser = naverOAuth2UserFeignClient.getUserInfo("Bearer " + accessToken);
        return naverUser.toOAuth2UserResponse();
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.NAVER;
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }
}
