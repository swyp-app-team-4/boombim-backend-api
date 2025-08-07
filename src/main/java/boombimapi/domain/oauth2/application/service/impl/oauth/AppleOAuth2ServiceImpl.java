package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.infra.AppleJwtUtils;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.apple.AppleTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;
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
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://appleid.apple.com/auth/authorize");
        urlBuilder.append("?client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        urlBuilder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        urlBuilder.append("&response_type=code");
        urlBuilder.append("&scope=").append(URLEncoder.encode("name email", StandardCharsets.UTF_8));
        urlBuilder.append("&response_mode=form_post");
        return urlBuilder.toString();
    }

    @Override
    public KakaoTokenResponse getTokens(String code) {
        String clientSecret = appleJwtUtils.generateClientSecret();

        AppleTokenResponse appleResponse = appleOAuth2FeignClient.getAccessToken(
                "authorization_code", clientId, clientSecret, code, redirectUri
        );

        log.info("====== Apple 토큰 정보 ======");
        log.info("Access Token: {}", appleResponse.accessToken());
        log.info("Refresh Token: {}", appleResponse.refreshToken());
        log.info("ID Token: {}", appleResponse.idToken());
        log.info("Expires In: {}", appleResponse.expiresIn());
        log.info("=============================");

        log.info("Apple 토큰 응답 수신 완료");
        log.debug("Access Token 존재: {}", appleResponse.accessToken() != null);
        log.debug("ID Token 존재: {}", appleResponse.idToken() != null);

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
    public KakaoUserResponse getUserInfo(String accessToken) {
        // Apple의 경우 Access Token으로는 사용자 정보를 가져올 수 없음
        // ID Token을 사용해야 함
        throw new UnsupportedOperationException("Apple은 Access Token으로 사용자 정보를 가져올 수 없습니다. ID Token을 사용하세요.");
    }

    @Override
    public KakaoUserResponse getUserInfoFromIdToken(String idToken) {
        try {
            log.info("Apple ID Token 파싱 시작");
            return parseIdToken(idToken);
        } catch (Exception e) {
            log.error("Apple ID Token 파싱 실패", e);
            throw new RuntimeException("Apple ID Token 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateToken(String accessToken) {
        // Apple의 경우 Access Token 검증은 복잡하므로 기본적으로 true 반환
        // 실제로는 ID Token의 유효성을 검증해야 함
        log.warn("Apple Access Token 검증은 구현되지 않았습니다. ID Token 검증을 사용하세요.");
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    @Override
    public KakaoTokenResponse convertToTokenResponse(SocialTokenRequest tokenRequest) {
        return new KakaoTokenResponse(
                tokenRequest.accessToken(),
                tokenRequest.refreshToken(),
                tokenRequest.expiresIn()
        );
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    private KakaoUserResponse parseIdToken(String idToken) {
        try {
            log.debug("파싱할 ID Token 길이: {}", idToken != null ? idToken.length() : "null");

            if (idToken == null || idToken.trim().isEmpty()) {
                throw new RuntimeException("ID Token이 null이거나 비어있습니다");
            }

            // JWT의 payload 부분을 디코딩
            String[] tokenParts = idToken.split("\\.");
            log.debug("JWT 토큰 파트 개수: {}", tokenParts.length);

            if (tokenParts.length != 3) {
                log.error("JWT 토큰 형식이 잘못됨. 파트 개수: {}", tokenParts.length);
                throw new RuntimeException("Invalid JWT token format - expected 3 parts but got " + tokenParts.length);
            }

            // Base64 URL 디코딩으로 payload 추출
            String payload;
            try {
                String payloadPart = tokenParts[1];
                log.debug("페이로드 파트 길이: {}", payloadPart.length());

                // Base64 URL 디코딩을 위한 패딩 추가
                while (payloadPart.length() % 4 != 0) {
                    payloadPart += "=";
                }

                byte[] decodedBytes = Base64.getUrlDecoder().decode(payloadPart);
                payload = new String(decodedBytes, StandardCharsets.UTF_8);
                log.debug("디코딩된 페이로드 길이: {}", payload.length());
            } catch (Exception e) {
                log.error("Base64 디코딩 실패", e);
                throw new RuntimeException("Base64 decoding failed", e);
            }

            // Jackson을 사용하여 JSON 파싱
            JsonNode claims = objectMapper.readTree(payload);

            String sub = claims.has("sub") ? claims.get("sub").asText() : null;
            String email = claims.has("email") ? claims.get("email").asText() : null;
            String name = null;

            // name 클레임이 있는지 확인
            if (claims.has("name")) {
                name = claims.get("name").asText();
            }

            // Apple은 이름 정보를 따로 제공하지 않는 경우가 많으므로 email에서 추출
            if (name == null && email != null && email.contains("@")) {
                name = email.split("@")[0];
            }

            if (sub == null) {
                log.error("Apple User ID (sub)가 클레임에 없습니다. 사용 가능한 클레임: {}", claims.fieldNames());
                throw new RuntimeException("Apple User ID (sub)가 없습니다");
            }

            log.info("Apple 사용자 정보 파싱 완료: userId={}, email={}, name={}", sub, email, name);

            return new KakaoUserResponse(
                    sub,
                    new KakaoUserResponse.KakaoAccount(
                            new KakaoUserResponse.Profile(
                                    name != null ? name : "Apple User",
                                    null // Apple은 프로필 이미지 제공하지 않음
                            ),
                            email
                    )
            );
        } catch (Exception e) {
            log.error("Apple ID Token 파싱 실패", e);
            throw new RuntimeException("Apple ID Token parsing failed: " + e.getMessage(), e);
        }
    }
}