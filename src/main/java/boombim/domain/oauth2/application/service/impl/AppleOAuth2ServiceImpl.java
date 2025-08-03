package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.OAuth2Service;
import boombim.domain.oauth2.domain.entity.SocialProvider;
import boombim.domain.oauth2.infra.AppleJwtUtils;
import boombim.domain.oauth2.presentation.dto.response.apple.AppleTokenResponse;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
@RequiredArgsConstructor
public class AppleOAuth2ServiceImpl implements OAuth2Service {
    private final AppleOAuth2FeignClient appleOAuth2FeignClient;
    private final AppleJwtUtils appleJwtUtils;

    @Value("${oauth2.apple.client-id}")
    private String clientId;

    @Value("${oauth2.apple.redirect-uri}")
    private String redirectUri;

    @Override
    public String getLoginUrl() {
        return "https://appleid.apple.com/auth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=name email" +
                "&response_mode=form_post";
    }

    @Override
    public OAuth2TokenResponse getTokens(String code) {
        String clientSecret = appleJwtUtils.generateClientSecret();

        AppleTokenResponse appleResponse = appleOAuth2FeignClient.getAccessToken(
                "authorization_code", clientId, clientSecret, code, redirectUri
        );

        return new OAuth2TokenResponse(
                appleResponse.accessToken(),
                appleResponse.refreshToken(),
                appleResponse.expiresIn()
        );
    }

    @Override
    public OAuth2TokenResponse refreshTokens(String refreshToken) {
        String clientSecret = appleJwtUtils.generateClientSecret();

        AppleTokenResponse appleResponse = appleOAuth2FeignClient.refreshToken(
                "refresh_token", clientId, clientSecret, refreshToken
        );

        return new OAuth2TokenResponse(
                appleResponse.accessToken(),
                appleResponse.refreshToken(),
                appleResponse.expiresIn()
        );
    }

    @Override
    public OAuth2UserResponse getUserInfo(String accessToken) {
        // Apple은 ID Token에서 사용자 정보를 추출
        // JWT 디코딩을 통해 사용자 정보 파싱
        return parseIdToken(accessToken);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    private OAuth2UserResponse parseIdToken(String idToken) {
        // Apple ID Token 파싱 로직 구현
        // JWT 디코딩 및 사용자 정보 추출
    }
}
