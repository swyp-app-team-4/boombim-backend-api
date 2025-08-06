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

    // Apple 토큰 응답을 저장할 ThreadLocal 변수
    private final ThreadLocal<AppleTokenResponse> currentAppleTokenResponse = new ThreadLocal<>();

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

        // Apple 토큰 응답을 ThreadLocal에 저장
        currentAppleTokenResponse.set(appleResponse);

        log.info("Apple 토큰 응답 수신 완료");
        log.debug("Access Token 존재: {}", appleResponse.accessToken() != null);
        log.debug("ID Token 존재: {}", appleResponse.idToken() != null);
        log.debug("ID Token 길이: {}", appleResponse.idToken() != null ? appleResponse.idToken().length() : 0);

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
        // ThreadLocal에서 저장된 Apple 토큰 응답 가져오기
        AppleTokenResponse appleTokenResponse = currentAppleTokenResponse.get();

        if (appleTokenResponse == null || appleTokenResponse.idToken() == null) {
            log.error("Apple ID Token이 없습니다. ThreadLocal에서 AppleTokenResponse를 찾을 수 없음");
            throw new RuntimeException("Apple ID Token을 찾을 수 없습니다");
        }

        try {
            log.info("Apple ID Token 파싱 시작");
            return parseIdToken(appleTokenResponse.idToken());
        } finally {
            // ThreadLocal 정리
            currentAppleTokenResponse.remove();
        }
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
                log.debug("토큰 앞부분: {}", idToken.length() > 100 ? idToken.substring(0, 100) + "..." : idToken);
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
                log.debug("디코딩된 페이로드 일부: {}", payload.length() > 200 ? payload.substring(0, 200) + "..." : payload);
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