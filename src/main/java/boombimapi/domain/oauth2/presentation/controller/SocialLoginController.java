package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.SocialLoginService;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.req.SocialTokenRequest;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Social Login", description = "소셜 로그인 API")
public class SocialLoginController {

    private final SocialLoginService socialLoginService;

    @Operation(summary = "소셜 로그인 URL 조회", description = "각 플랫폼별 소셜 로그인 URL을 반환합니다. (테스트용)")
    @GetMapping("/login/{provider}")
    public ResponseEntity<String> getLoginUrl(@PathVariable SocialProvider provider) {
        log.info("소셜 로그인 URL 요청: {}", provider);
        String loginUrl = socialLoginService.getLoginUrl(provider);
        return ResponseEntity.ok(loginUrl);
    }

    @Operation(summary = "소셜 토큰으로 로그인", description = "앱에서 받은 소셜 토큰으로 로그인을 처리합니다.")
    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginToken> socialLoginWithToken(
            @PathVariable SocialProvider provider,
            @RequestBody SocialTokenRequest request) {

        log.info("소셜 토큰 로그인: provider={}", provider);

        LoginToken loginToken = socialLoginService.loginWithToken(provider, request);

        log.info("✅✅ACToken={}", loginToken.accessToken());
        log.info("✅✅RFToken={}", loginToken.refreshToken());

        return ResponseEntity.ok(loginToken);
    }

    // 기존 콜백 방식은 테스트용으로 유지 (필요시 제거 가능)
    @Operation(summary = "소셜 로그인 콜백 (테스트용)", description = "테스트용 콜백 API")
    @GetMapping("/callback/{provider}")
    public ResponseEntity<LoginToken> socialLogin(
            @PathVariable SocialProvider provider,
            @RequestParam("code") String code) {

        log.info("소셜 로그인: provider={}, code={}", provider, code);
        LoginToken loginToken = socialLoginService.login(provider, code);
        return ResponseEntity.ok(loginToken);
    }
}