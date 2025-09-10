package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.naver.NaverTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.naver.NaverUserResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.feignclient.naver.NaverOAuth2URLFeignClient;
import boombimapi.global.infra.feignclient.naver.NaverOAuth2UserFeignClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
        String state = generateState();
        return baseUrl +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + state;
    }

    @Override
    public KakaoTokenResponse getTokens(String code) {
        NaverTokenResponse naverResponse = naverOAuth2URLFeignClient.getAccessToken(
                "authorization_code", clientId, clientSecret, redirectUri, code, "state_value"
        );

        return new KakaoTokenResponse(
                naverResponse.accessToken(),
                naverResponse.refreshToken(),
                "idnull",
                naverResponse.expiresIn()
        );
    }

    @Override
    public KakaoTokenResponse refreshTokens(String refreshToken) {
        NaverTokenResponse naverResponse = naverOAuth2URLFeignClient.refreshToken(
                "refresh_token", clientId, clientSecret, refreshToken
        );

        return new KakaoTokenResponse(
                naverResponse.accessToken(),
                naverResponse.refreshToken(),
                "idnull",
                naverResponse.expiresIn()
        );
    }

    @Override
    public KakaoUserResponse getUserInfo(String accessToken) {
        try {
            NaverUserResponse naverUser = naverOAuth2UserFeignClient.getUserInfo("Bearer " + accessToken);
            return naverUser.toOAuth2UserResponse();
        } catch (Exception e) {
            log.error("네이버 사용자 정보 조회 실패: {}", e.getMessage());
            throw new BoombimException(ErrorCode.INVALID_PROVIDER);
        }
    }

    @Override
    public KakaoUserResponse getUserInfoFromIdToken(String idToken) {
        // 네이버는 ID Token을 사용하지 않으므로 지원하지 않음
        throw new UnsupportedOperationException("네이버는 ID Token을 지원하지 않습니다");
    }

    @Override
    public boolean validateToken(String accessToken) {
        try {
            // 사용자 정보 조회를 통해 토큰 유효성 검증
            KakaoUserResponse userInfo = getUserInfo(accessToken);
            return userInfo != null && userInfo.id() != null;
        } catch (Exception e) {
            log.error("네이버 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public KakaoTokenResponse convertToTokenResponse(SocialTokenRequest tokenRequest) {
        return new KakaoTokenResponse(
                tokenRequest.accessToken(),
                tokenRequest.refreshToken(),
                "idnull",
                tokenRequest.expiresIn()
        );
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.NAVER;
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }
}