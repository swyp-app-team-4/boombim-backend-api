package boombimapi.domain.oauth2.application.service.impl.oauth;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.oauth2.application.service.CreateAccessTokenAndRefreshTokenService;
import boombimapi.domain.oauth2.application.service.OAuth2Service;
import boombimapi.domain.oauth2.application.service.SocialLoginService;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoTokenResponse;
import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;
import boombimapi.domain.member.domain.entity.Role;

import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.jwt.domain.entity.SocialToken;
import boombimapi.global.jwt.domain.repository.SocialTokenRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SocialLoginServiceImpl implements SocialLoginService {

    private final java.util.Map<SocialProvider, OAuth2Service> oauth2Services;
    private final CreateAccessTokenAndRefreshTokenService tokenService;
    private final MemberRepository userRepository;
    private final SocialTokenRepository socialTokenRepository;

    @Value("${oauth2.apple.profile}")
    private String appleProfile;

    public SocialLoginServiceImpl(List<OAuth2Service> oauth2Services,
                                  CreateAccessTokenAndRefreshTokenService tokenService,
                                  MemberRepository userRepository,
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
    public LoginToken loginWithToken(SocialProvider provider, SocialTokenRequest tokenRequest) {
        OAuth2Service oauth2Service = getOAuth2Service(provider);

        log.info("소셜 토큰으로 로그인 시작: provider={}", provider);

        // Apple일 경우 idToken 검증, 나머지는 accessToken 검증
        boolean isValidToken = (provider == SocialProvider.APPLE)
                ? oauth2Service.validateToken(tokenRequest.idToken())
                : oauth2Service.validateToken(tokenRequest.accessToken());

        if (!isValidToken) {
            throw new BoombimException(ErrorCode.INVALID_PARAMETER, "유효하지 않은 토큰입니다");
        }

        // 2. 사용자 정보 획득
        KakaoUserResponse userResponse;
        if (provider == SocialProvider.APPLE && tokenRequest.idToken() != null) {
            // Apple의 경우 ID Token으로 사용자 정보 파싱
            userResponse = oauth2Service.getUserInfoFromIdToken(tokenRequest.idToken());
        } else {
            // 다른 플랫폼은 Access Token으로 사용자 정보 조회
            userResponse = oauth2Service.getUserInfo(tokenRequest.accessToken());
        }

        log.info("사용자 정보 획득 완료: userId={}, provider={}", userResponse.id(), provider);

        // 3. 토큰 정보를 KakaoTokenResponse 형태로 변환
        KakaoTokenResponse tokenResponse = oauth2Service.convertToTokenResponse(tokenRequest);

        // 4. 사용자 생성 또는 업데이트
        Member user = createSocialUser(provider, tokenResponse, userResponse);

        // 5. JWT 토큰 생성 및 반환
        return tokenService.createAccessTokenAndRefreshToken(
                user.getId(),
                user.getRole(),
                user.getEmail()
        );
    }

    @Override
    public LoginToken login(SocialProvider provider, String code) {
        // 기존 Authorization Code Flow 방식 (테스트용)
        OAuth2Service oauth2Service = getOAuth2Service(provider);

        KakaoTokenResponse tokenResponse = oauth2Service.getTokens(code);
        log.info("====== 소셜에서 받은 토큰 정보 ======");
        log.info("Access Token: {}", tokenResponse.accessToken());
        log.info("Refresh Token: {}", tokenResponse.refreshToken());
        log.info("Expires In: {}", tokenResponse.expiresIn());
        log.info("===================================");


        log.info("토큰 획득 완료: provider={}", provider);


        KakaoUserResponse userResponse = oauth2Service.getUserInfo(tokenResponse.accessToken());
        log.info("사용자 정보 획득 완료: userId={}, provider={}", userResponse.id(), provider);

        Member user = createSocialUser(provider, tokenResponse, userResponse);

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

    private Member createSocialUser(SocialProvider provider,
                                    KakaoTokenResponse tokenResponse,
                                    KakaoUserResponse userResponse) {
        log.info("소셜 사용자 생성 시작: provider={}, userId={}", provider, userResponse.id());

        if (userResponse.id() == null || userResponse.id().isEmpty()) {
            log.error("소셜 로그인에서 반환된 사용자 ID가 null 또는 빈 문자열입니다");
            throw new IllegalArgumentException("사용자 ID가 유효하지 않습니다");
        }

        Member user = userRepository.findById(userResponse.id()).orElse(null);


        if (user == null) {
            Optional<Member> existingUserByEmail = userRepository.findByEmail(userResponse.getEmail());
            if (existingUserByEmail.isPresent()) {
                log.error("이미 사용 중인 이메일입니다: {}", userResponse.getEmail());
                throw new BoombimException(ErrorCode.DUPLICATE_EMAIL);
            }

            log.info("신규 {} 사용자 생성: {}", provider, userResponse.getName());
            if (provider == SocialProvider.APPLE) {
                user = Member.builder()
                        .id(userResponse.id())
                        .email(userResponse.getEmail())
                        .name(userResponse.getName())
                        .profile(appleProfile)
                        .socialProvider(provider)
                        .role(Role.USER)
                        .build();
            } else {
                user = Member.builder()
                        .id(userResponse.id())
                        .email(userResponse.getEmail())
                        .name(userResponse.getName())
                        .profile(userResponse.getProfile())
                        .socialProvider(provider)
                        .role(Role.USER)
                        .build();
            }

            userRepository.save(user);
        } else {
            if (!user.getEmail().equals(userResponse.getEmail())) {
                Optional<Member> existingUserByEmail = userRepository.findByEmail(userResponse.getEmail());
                if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(user.getId())) {
                    log.error("변경하려는 이메일이 이미 사용 중입니다: {}", userResponse.getEmail());
                    throw new BoombimException(ErrorCode.DUPLICATE_EMAIL);
                }
            }
            log.info("기존 {} 사용자 정보 업데이트: {}", provider, userResponse.getName());
            user.updateEmailAndProfile(userResponse.getEmail(), userResponse.getProfile());
            user.updateIsActivateNameFlag();
        }

        saveSocialToken(user.getId(), provider, tokenResponse);
        return user;
    }

    private void saveSocialToken(String userId, SocialProvider provider, KakaoTokenResponse tokenResponse) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(
                tokenResponse.expiresIn() != null ? tokenResponse.expiresIn() : 3600L
        );

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

        log.info("소셜 토큰 저장 완료: userId={}, provider={}", userId, provider);
    }
}