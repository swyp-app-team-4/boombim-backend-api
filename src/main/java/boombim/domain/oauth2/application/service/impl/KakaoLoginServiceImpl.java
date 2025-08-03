package boombim.domain.oauth2.application.service.impl;


import boombim.domain.oauth2.application.service.*;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import boombim.domain.user.domain.entity.Role;
import boombim.global.jwt.domain.entity.KakaoJsonWebToken;
import boombim.global.jwt.domain.repository.KakaoJsonWebTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoLoginServiceImpl implements KakaoLoginService {

    @Value("${oauth2.front-uri}")
    private String redirectUri;

    private final KakaoAccessTokenAndRefreshTokenService KakaoAccessTokenAndRefreshTokenService;
    private final KakaoUserService KakaoUserService;
    private final CreateAccessTokenAndRefreshTokenService createAccessTokenAndRefreshTokenService;
    private final KakaoUserCreateService KakaoUserCreateService;
    private final KakaoJsonWebTokenRepository kakaoJsonWebTokenRepository;

    @Override
    public String login(String code, HttpServletResponse response) {
        OAuth2TokenResponse oAuth2TokenResponse = KakaoAccessTokenAndRefreshTokenService.getAccessTokenAndRefreshToken(code);

        OAuth2UserResponse oAuth2UserResponse = KakaoUserService.getUser(oAuth2TokenResponse.accessToken());

        Map<String, String> values = KakaoUserCreateService.createKakaoUser(oAuth2TokenResponse, oAuth2UserResponse);

        String userId = values.get("id");
        Role role = Role.valueOf(values.get("role"));
        String userEmail = values.get("email");

        // Kakao 토큰 저장
        KakaoJsonWebToken KakaoToken = KakaoJsonWebToken.builder()
                .userId(userId)
                .accessToken(oAuth2TokenResponse.accessToken())
                .refreshToken(oAuth2TokenResponse.refreshToken())
                .build();
        kakaoJsonWebTokenRepository.save(KakaoToken);

        // 자체 access/refresh 토큰 생성
        Map<String, String> tokens = createAccessTokenAndRefreshTokenService.createAccessTokenAndRefreshToken(userId, role, userEmail);
        String accessToken = tokens.get("access_token");
        String refreshTokenCookie = tokens.get("refresh_token_cookie"); // ex) refreshToken=xxx; Path=/; HttpOnly

        // 쿠키 설정
        response.addHeader("Set-Cookie", refreshTokenCookie);
        log.info("✅ 엑세스:{}", accessToken );
        log.info("✅ 리프레쉬:{}", refreshTokenCookie);

        //  프론트엔드 리다이렉트 URL 생성 (accessToken만 전달)
        return redirectUri + "?accessToken=" + accessToken;
//        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("access_token"));
//        response.addHeader(HttpHeaders.SET_COOKIE, tokens.get("refresh_token_cookie"));

//        response.setStatus(HttpServletResponse.SC_OK);
//        response.setContentType("application/json");

//        response.getWriter().write("Successfully Login");

    }
}
