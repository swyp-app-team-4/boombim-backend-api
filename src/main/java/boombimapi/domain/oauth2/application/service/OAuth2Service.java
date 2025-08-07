package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;

public interface OAuth2Service {

    // 기존 Authorization Code Flow 방식 (테스트용)
    String getLoginUrl();
    KakaoTokenResponse getTokens(String code);
    KakaoTokenResponse refreshTokens(String refreshToken);

    // 새로운 방식: 토큰으로 사용자 정보 조회
    KakaoUserResponse getUserInfo(String accessToken);

    // Apple의 경우 ID Token으로 사용자 정보 조회
    KakaoUserResponse getUserInfoFromIdToken(String idToken);

    // 토큰 검증 (소셜 플랫폼에서 토큰이 유효한지 확인)
    boolean validateToken(String accessToken);

    // 토큰을 KakaoTokenResponse 형태로 변환
    KakaoTokenResponse convertToTokenResponse(SocialTokenRequest tokenRequest);

    SocialProvider getProvider();
}