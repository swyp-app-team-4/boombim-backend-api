package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.infra.AppleJwtUtils;
import boombimapi.domain.oauth2.presentation.dto.response.apple.AppleTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoUserResponse;
import boombimapi.global.infra.feignclient.ios.AppleOAuth2FeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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
    public KakaoTokenResponse getTokens(String code) {
        String clientSecret = appleJwtUtils.generateClientSecret();

        AppleTokenResponse appleResponse = appleOAuth2FeignClient.getAccessToken(
                "authorization_code", clientId, clientSecret, code, redirectUri
        );

        return new KakaoTokenResponse(
                appleResponse.accessToken(),
                appleResponse.refreshToken(),
                appleResponse.expiresIn()
        );
    }

    @Override
    public KakaoTokenResponse refreshTokens(String refreshToken) {
        String clientSecret = appleJwtUtils.generateClientSecret();

        AppleTokenResponse appleResponse = appleOAuth2FeignClient.refreshToken(
                "refresh_token", clientId, clientSecret, refreshToken
        );

        return new KakaoTokenResponse(
                appleResponse.accessToken(),
                appleResponse.refreshToken(),
                appleResponse.expiresIn()
        );
    }

    @Override
    public KakaoUserResponse getUserInfo(String idToken) {
        // Apple은 ID Token에서 사용자 정보를 추출
        return parseIdToken(idToken);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    private KakaoUserResponse parseIdToken(String idToken) {
        try {
            // JWT의 payload 부분을 디코딩
            String[] tokenParts = idToken.split("\\.");
            if (tokenParts.length != 3) {
                throw new RuntimeException("Invalid JWT token format");
            }

            // Base64 URL 디코딩으로 payload 추출
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));

            // Jackson을 사용하여 JSON 파싱
            JsonNode claims = objectMapper.readTree(payload);

            String sub = claims.get("sub").asText(); // Apple User ID
            String email = claims.has("email") ? claims.get("email").asText() : null;
            String name = claims.has("name") ? claims.get("name").asText() : null;

            return new KakaoUserResponse(
                    sub,
                    new KakaoUserResponse.KakaoAccount(
                            new KakaoUserResponse.Profile(
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