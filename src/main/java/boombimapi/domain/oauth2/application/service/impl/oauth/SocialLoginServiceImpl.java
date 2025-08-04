package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.oauth2.application.service.CreateAccessTokenAndRefreshTokenService;
import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.application.service.SocialLoginService;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.response.LoginToken;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoUserResponse;
import boombimapi.domain.user.domain.entity.Role;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.user.domain.repository.UserRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.jwt.domain.entity.SocialToken;
import boombimapi.global.jwt.domain.repository.SocialTokenRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SocialLoginServiceImpl implements SocialLoginService {

    private final java.util.Map<SocialProvider, OAuth2Service> oauth2Services;
    private final CreateAccessTokenAndRefreshTokenService tokenService;
    private final UserRepository userRepository;
    private final SocialTokenRepository socialTokenRepository;


    // 로그 좀 볼려고 넣었습니다.
    public SocialLoginServiceImpl(List<OAuth2Service> oauth2Services,
                                  CreateAccessTokenAndRefreshTokenService tokenService,
                                  UserRepository userRepository,
                                  SocialTokenRepository socialTokenRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.socialTokenRepository = socialTokenRepository;

        log.info("=== OAuth2Service 목록 ===");
        oauth2Services.forEach(service -> {
            log.info("OAuth2Service: {}, Provider: {}", service.getClass().getSimpleName(), service.getProvider());
        });

        this.oauth2Services = oauth2Services.stream()
                .collect(Collectors.toMap(
                        OAuth2Service::getProvider,
                        Function.identity()
                ));

        log.info("=== OAuth2Services 매핑 결과 ===");
        this.oauth2Services.forEach((provider, service) -> {
            log.info("Provider: {}, Service: {}", provider, service.getClass().getSimpleName());
        });
    }

    @Override
    public LoginToken login(SocialProvider provider, String code) {
        OAuth2Service oauth2Service = getOAuth2Service(provider);

        KakaoTokenResponse tokenResponse = oauth2Service.getTokens(code);
        KakaoUserResponse userResponse = oauth2Service.getUserInfo(tokenResponse.accessToken());

        User user = createSocialUser(provider, tokenResponse, userResponse);

        return tokenService.createAccessTokenAndRefreshToken(
                user.getId(),
                user.getRole(),
                user.getEmail()
        );
    }

    @Override
    public String getLoginUrl(SocialProvider provider) {
        log.info("로그인 URL 요청 - Provider: {}", provider);
        OAuth2Service oauth2Service = getOAuth2Service(provider);
        String loginUrl = oauth2Service.getLoginUrl();
        log.info("생성된 로그인 URL: {}", loginUrl);
        return loginUrl;
    }

    private OAuth2Service getOAuth2Service(SocialProvider provider) {
        log.info("OAuth2Service 조회 - Provider: {}", provider);
        log.info("사용 가능한 Provider들: {}", oauth2Services.keySet());

        OAuth2Service service = oauth2Services.get(provider);
        if (service == null) {
            log.error("Provider {}에 대한 OAuth2Service를 찾을 수 없습니다. 사용 가능한 Provider: {}",
                    provider, oauth2Services.keySet());
            throw new BoombimException(ErrorCode.INVALID_PARAMETER);
        }

        log.info("찾은 OAuth2Service: {}", service.getClass().getSimpleName());
        return service;
    }


    private User createSocialUser(SocialProvider provider,
                                  KakaoTokenResponse tokenResponse,
                                  KakaoUserResponse userResponse) {
        log.info("소셜 사용자 생성 시작: provider={}, userId={}", provider, userResponse.id());

        if (userResponse.id() == null || userResponse.id().isEmpty()) {
            log.error("소셜 로그인에서 반환된 사용자 ID가 null 또는 빈 문자열입니다");
            throw new IllegalArgumentException("사용자 ID가 유효하지 않습니다");
        }

        User user = userRepository.findById(userResponse.id()).orElse(null);

        if (user == null) {
            log.info("신규 {} 사용자 생성: {}", provider, userResponse.getName());
            user = User.builder()
                    .id(userResponse.id())
                    .email(userResponse.getEmail())
                    .name(userResponse.getName())
                    .profile(userResponse.getProfile())
                    .socialProvider(provider)
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        } else {
            user.updateEmailAndProfile(userResponse.getEmail(), userResponse.getProfile());
        }

        saveSocialToken(user.getId(), provider, tokenResponse);
        return user;
    }

    private void saveSocialToken(String userId, SocialProvider provider, KakaoTokenResponse tokenResponse) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.expiresIn());

        SocialToken socialToken = SocialToken.builder()
                .id(SocialToken.generateId(userId, provider))
                .userId(userId)
                .provider(provider)
                .accessToken(tokenResponse.accessToken())
                .refreshToken(tokenResponse.refreshToken())
                .expiresIn(expiresAt)
                .build();

        socialTokenRepository.deleteByUserIdAndProvider(userId, provider);
        socialTokenRepository.save(socialToken);
    }
}