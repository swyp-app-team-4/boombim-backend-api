package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.SocialLoginService;
import boombimapi.domain.oauth2.cookie.AuthCookieManager;
import boombimapi.domain.oauth2.cookie.vo.AuthCookies;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import boombimapi.global.properties.CookieProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Social Login", description = "소셜 로그인 API")
public class SocialLoginController {

    private final CookieProperties cookieProperties;
    private final AuthCookieManager authCookieManager;
    private final SocialLoginService socialLoginService;

    @Operation(summary = "[WEB] 소셜 로그인 시작", description = "웹에서 소셜 로그인을 시작합니다.")
    @GetMapping("/web/oauth2/login/{provider}")
    public ResponseEntity<Void> redirectToProvider(
        @PathVariable SocialProvider provider
    ) {
        log.info("{} 로그인 시작", provider);
        final String loginUrl = socialLoginService.getLoginUrl(provider);
        return ResponseEntity.status(302)
            .header("Location", loginUrl)
            .build();
    }

    @Operation(summary = "[WEB] 소셜 로그인 콜백", description = "쿠키(HttpOnly)로 AT/RT 세팅 후 프론트로 리다이렉트합니다.")
    @GetMapping("/web/oauth2/callback/{provider}")
    public ResponseEntity<Void> socialWebLogin(
        @PathVariable SocialProvider provider,
        @RequestParam("code") String code
    ) {
        LoginToken loginToken = socialLoginService.login(provider, code);

        AuthCookies authCookies = authCookieManager.createAuthCookies(
            loginToken.accessToken(),
            loginToken.refreshToken()
        );

        return ResponseEntity.status(302)
            .header("Location", cookieProperties.frontRedirect())
            .header("Set-Cookie", authCookies.accessTokenCookie().toString())
            .header("Set-Cookie", authCookies.refreshTokenCookie().toString())
            .build();
    }

    @Operation(summary = "[APP] 소셜 토큰으로 로그인", description = "앱에서 받은 소셜 토큰으로 로그인을 처리합니다.")
    @PostMapping("/app/oauth2/login/{provider}")
    public ResponseEntity<LoginToken> socialLoginWithToken(
        @PathVariable SocialProvider provider,
        @RequestBody SocialTokenRequest request) {

        log.info("소셜 토큰 로그인: provider={}", provider);

        LoginToken loginToken = socialLoginService.loginWithToken(provider, request);

        log.info("✅✅ACToken={}", loginToken.accessToken());
        log.info("✅✅RFToken={}", loginToken.refreshToken());

        return ResponseEntity.ok(loginToken);
    }

    @Operation(summary = "[TEST] 소셜 로그인 URL 조회", description = "각 플랫폼별 소셜 로그인 URL을 반환합니다. (테스트용)")
    @GetMapping("/test/oauth2/login/{provider}")
    public ResponseEntity<String> getLoginUrl(@PathVariable SocialProvider provider) {
        log.info("소셜 로그인 URL 요청: {}", provider);
        String loginUrl = socialLoginService.getLoginUrl(provider);
        return ResponseEntity.ok(loginUrl);
    }

    // 기존 콜백 방식은 테스트용으로 유지 (필요시 제거 가능)
    @Operation(summary = "[TEST] 소셜 로그인 콜백", description = "테스트용 콜백 API")
    @GetMapping("/callback/{provider}")
    public ResponseEntity<LoginToken> socialLogin(
        @PathVariable SocialProvider provider,
        @RequestParam("code") String code) {

        log.info("소셜 로그인: provider={}, code={}", provider, code);
        LoginToken loginToken = socialLoginService.login(provider, code);
        return ResponseEntity.ok(loginToken);
    }

    @Operation(summary = "[TEST] 소셜 로그인 콜백", description = "테스트용 콜백 API")
    @PostMapping("/callback/apple")
    public ResponseEntity<LoginToken> socialAppleLogin(
        @RequestParam("code") String code) {

        LoginToken loginToken = socialLoginService.login(SocialProvider.APPLE, code);
        return ResponseEntity.ok(loginToken);
    }
}