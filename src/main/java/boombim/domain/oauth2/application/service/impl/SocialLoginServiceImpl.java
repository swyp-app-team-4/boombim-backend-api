package boombim.domain.oauth2.application.service.impl;


import boombim.domain.oauth2.application.service.SocialLoginService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SocialLoginServiceImpl implements SocialLoginService {

    private final Map<SocialProvider, OAuth2Service> oauth2Services;
    private final CreateAccessTokenAndRefreshTokenService tokenService;
    private final SocialUserCreateService userCreateService;
    private final SocialTokenRepository socialTokenRepository;

    @Value("${oauth2.front-uri}")
    private String frontRedirectUri;

    public SocialLoginServiceImpl(List<OAuth2Service> oauth2Services,
                                  CreateAccessTokenAndRefreshTokenService tokenService,
                                  SocialUserCreateService userCreateService,
                                  SocialTokenRepository socialTokenRepository) {
        this.tokenService = tokenService;
        this.userCreateService = userCreateService;
        this.socialTokenRepository = socialTokenRepository;

        // OAuth2Service 구현체들을 Provider별로 매핑
        this.oauth2Services = oauth2Services.stream()
                .collect(Collectors.toMap(
                        OAuth2Service::getProvider,
                        Function.identity()
                ));
    }

    @Override
    public String login(SocialProvider provider, String code, HttpServletResponse response) {
        OAuth2Service oauth2Service = getOAuth2Service(provider);

        // 1. Authorization Code로 토큰 획득
        OAuth2TokenResponse tokenResponse = oauth2Service.getTokens(code);

        // 2. Access Token으로 사용자 정보 획득
        OAuth2UserResponse userResponse = oauth2Service.getUserInfo(tokenResponse.accessToken());

        // 3. 사용자 생성/업데이트 및 소셜 토큰 저장
        Map<String, String> userInfo = userCreateService.createSocialUser(
                provider, tokenResponse, userResponse
        );

        // 4. 자체 JWT 토큰 생성
        Map<String, String> tokens = tokenService.createAccessTokenAndRefreshToken(
                userInfo.get("id"),
                Role.valueOf(userInfo.get("role")),
                userInfo.get("email")
        );

        // 5. 쿠키 설정
        response.addHeader("Set-Cookie", tokens.get("refresh_token_cookie"));

        // 6. 프론트엔드로 리다이렉트
        return frontRedirectUri + "?accessToken=" + tokens.get("access_token");
    }

    @Override
    public String getLoginUrl(SocialProvider provider) {
        OAuth2Service oauth2Service = getOAuth2Service(provider);
        return oauth2Service.getLoginUrl();
    }

    private OAuth2Service getOAuth2Service(SocialProvider provider) {
        OAuth2Service service = oauth2Services.get(provider);
        if (service == null) {
            throw new BabbuddyException(ErrorCode.UNSUPPORTED_PROVIDER);
        }
        return service;
    }
}