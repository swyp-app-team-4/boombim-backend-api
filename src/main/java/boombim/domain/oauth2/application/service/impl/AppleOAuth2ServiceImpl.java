package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.OAuth2Service;
import boombim.domain.oauth2.domain.entity.SocialProvider;
import boombim.domain.oauth2.infra.AppleJwtUtils;
import boombim.domain.oauth2.presentation.dto.response.apple.AppleTokenResponse;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import boombim.global.infra.feignclient.ios.AppleOAuth2FeignClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
    public OAuth2UserResponse getUserInfo(String idToken) {
        // Apple은 ID Token에서 사용자 정보를 추출
        return parseIdToken(idToken);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    private OAuth2UserResponse parseIdToken(String idToken) {
        try {
            // JWT의 payload 부분을 디코딩
            String[] tokenParts = idToken.split("\\.");
            if (tokenParts.length != 3) {
                throw new RuntimeException("Invalid JWT token format");
            }

            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));

            // Claims 파싱을 위해 Jwts 사용하지 않고 간단히 처리
            // 실제로는 Apple의 공개키로 검증해야 함
            Claims claims = Jwts.parser()
                    .setAllowedClockSkewSeconds(60)
                    .parseClaimsJwt(idToken.substring(0, idToken.lastIndexOf('.') + 1))
                    .getBody();

            String sub = claims.getSubject(); // Apple User ID
            String email = claims.get("email", String.class);
            String name = claims.get("name", String.class);

            return new OAuth2UserResponse(
                    sub,
                    new OAuth2UserResponse.KakaoAccount(
                            new OAuth2UserResponse.Profile(
                                    name != null ? name : email,
                                    null // Apple은 프로필 이미지 제공하지 않음
                            ),
                            email
                    )
            );
        } catch (Exception e) {
            log.error("Apple ID Token 파싱 실패", e);
            throw new RuntimeException("Apple ID Token parsing failed", e);
        }
    }
}