package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.infra.AppleJwtUtils;
import boombimapi.domain.oauth2.presentation.dto.response.apple.AppleTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoUserResponse;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
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
        String state = generateState(); // CSRF 방지를 위한 state 값

        return "https://appleid.apple.com/auth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("name email", StandardCharsets.UTF_8) +
                "&response_mode=form_post" +
                "&state=" + state;
    }

    @Override
    public KakaoTokenResponse getTokens(String code) {
        try {
            log.info("Apple 토큰 요청 시작 - code: {}", code != null ? "존재함" : "null");

            String clientSecret = appleJwtUtils.generateClientSecret();
            log.info("Client Secret 생성 완료");

            AppleTokenResponse appleResponse = appleOAuth2FeignClient.getAccessToken(
                    "authorization_code",
                    clientId,
                    clientSecret,
                    code,
                    redirectUri
            );

            // 에러 체크
            if (appleResponse.hasError()) {
                log.error("Apple 토큰 요청 에러: {}", appleResponse.getErrorMessage());
                throw new BoombimException(ErrorCode.AUTHORIZED_ERROR, "Apple 인증 실패: " + appleResponse.getErrorMessage());
            }

            log.info("Apple 토큰 응답 수신 완료");

            // Apple의 경우 사용자 정보가 ID Token에 있으므로 ID Token을 access token으로 사용
            return new KakaoTokenResponse(
                    appleResponse.getUserInfoToken(), // ID Token을 사용자 정보 조회용으로 사용
                    appleResponse.refreshToken(),
                    appleResponse.expiresIn()
            );
        } catch (BoombimException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 토큰 요청 실패: {}", e.getMessage(), e);
            throw new BoombimException(ErrorCode.AUTHORIZED_ERROR, "Apple 토큰 요청 실패: " + e.getMessage());
        }
    }

    @Override
    public KakaoTokenResponse refreshTokens(String refreshToken) {
        try {
            String clientSecret = appleJwtUtils.generateClientSecret();

            AppleTokenResponse appleResponse = appleOAuth2FeignClient.refreshToken(
                    "refresh_token",
                    clientId,
                    clientSecret,
                    refreshToken
            );

            return new KakaoTokenResponse(
                    appleResponse.accessToken(),
                    appleResponse.refreshToken(),
                    appleResponse.expiresIn()
            );
        } catch (Exception e) {
            log.error("Apple 토큰 갱신 실패: {}", e.getMessage(), e);
            throw new BoombimException(ErrorCode.AUTHORIZED_ERROR, "Apple 토큰 갱신 실패: " + e.getMessage());
        }
    }

    @Override
    public KakaoUserResponse getUserInfo(String accessToken) {
        // Apple의 경우 실제로는 ID Token에서 사용자 정보를 추출해야 합니다.
        // 여기서 accessToken은 실제로 ID Token입니다.
        return parseIdToken(accessToken);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    private KakaoUserResponse parseIdToken(String idToken) {
        try {
            log.info("Apple ID Token 파싱 시작");

            // JWT의 payload 부분을 디코딩
            String[] tokenParts = idToken.split("\\.");
            if (tokenParts.length != 3) {
                throw new RuntimeException("Invalid JWT token format");
            }

            // Base64 URL 디코딩으로 payload 추출
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            log.debug("ID Token payload: {}", payload);

            // Jackson을 사용하여 JSON 파싱
            JsonNode claims = objectMapper.readTree(payload);

            String sub = claims.get("sub").asText(); // Apple User ID
            String email = claims.has("email") ? claims.get("email").asText() : null;

            // Apple은 이름 정보를 ID Token에 포함하지 않는 경우가 많음
            // 최초 로그인 시에만 name 정보가 포함됨
            String name = null;
            if (claims.has("name")) {
                name = claims.get("name").asText();
            } else if (email != null) {
                // 이메일에서 사용자명 추출 (예: test@example.com -> test)
                name = email.split("@")[0];
            } else {
                name = "Apple User"; // 기본값
            }

            log.info("Apple 사용자 정보 파싱 완료 - sub: {}, email: {}, name: {}", sub, email, name);

            return new KakaoUserResponse(
                    sub,
                    new KakaoUserResponse.KakaoAccount(
                            new KakaoUserResponse.Profile(
                                    name,
                                    null // Apple은 프로필 이미지 제공하지 않음
                            ),
                            email
                    )
            );
        } catch (Exception e) {
            log.error("Apple ID Token 파싱 실패: {}", e.getMessage(), e);
            throw new BoombimException(ErrorCode.AUTHORIZED_ERROR, "Apple ID Token parsing failed: " + e.getMessage());
        }
    }

    private String generateState() {
        return java.util.UUID.randomUUID().toString();
    }
}