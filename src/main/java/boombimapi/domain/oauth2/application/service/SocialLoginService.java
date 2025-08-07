package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;

public interface SocialLoginService {

    // 새로운 방식: 앱에서 토큰을 직접 전달받아 로그인
    LoginToken loginWithToken(SocialProvider provider, SocialTokenRequest tokenRequest);

    // 기존 방식: Authorization Code로 로그인 (테스트용으로 유지)
    LoginToken login(SocialProvider provider, String code);

    // 로그인 URL 조회 (테스트용)
    String getLoginUrl(SocialProvider provider);
}