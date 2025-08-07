package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;
import boombimapi.global.infra.feignclient.kakao.KakaoOAuth2URLFeignClient;
import boombimapi.global.infra.feignclient.kakao.KakaoOAuth2UserFeignClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
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
    public KakaoTokenResponse getTokens(String code) {
        return kakaoOAuth2URLFeignClient.getAccessToken(
                code, clientId, clientSecret, redirectUri, "authorization_code"
        );
    }

    @Override
    public KakaoTokenResponse refreshTokens(String refreshToken) {
        return kakaoOAuth2URLFeignClient.refreshToken(
                "refresh_token", refreshToken, clientId, clientSecret
        );
    }

    @Override
    public KakaoUserResponse getUserInfo(String accessToken) {
        try {
            return kakaoOAuth2UserFeignClient.getUserInfo("Bearer " + accessToken);
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 사용자 정보 조회에 실패했습니다", e);
        }
    }

    @Override
    public KakaoUserResponse getUserInfoFromIdToken(String idToken) {
        // 카카오는 ID Token을 사용하지 않으므로 지원하지 않음
        throw new UnsupportedOperationException("카카오는 ID Token을 지원하지 않습니다");
    }

    @Override
    public boolean validateToken(String accessToken) {
        try {
            // 사용자 정보 조회를 통해 토큰 유효성 검증
            KakaoUserResponse userInfo = getUserInfo(accessToken);
            return userInfo != null && userInfo.id() != null;
        } catch (Exception e) {
            log.error("카카오 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
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
        return SocialProvider.KAKAO;
    }
}