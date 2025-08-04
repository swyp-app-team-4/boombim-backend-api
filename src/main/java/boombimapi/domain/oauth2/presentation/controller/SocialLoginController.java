package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.SocialLoginService;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.response.LoginToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Social Login", description = "소셜 로그인 API")
public class SocialLoginController {

    private final SocialLoginService socialLoginService;


    @Operation(summary = "소셜 로그인 URL 조회", description = "각 플랫폼별 소셜 로그인 URL을 반환합니다.")
    @GetMapping("/login/{provider}")
    public ResponseEntity<String> getLoginUrl(@PathVariable SocialProvider provider) {
        log.info("소셜 로그인 URL 요청: {}", provider);
        String loginUrl = socialLoginService.getLoginUrl(provider);
        return ResponseEntity.ok(loginUrl);
    }

    @Operation(summary = "소셜 로그인 콜백", description = "각 플랫폼에서 Authorization Code를 받아 로그인을 처리합니다.")
    @GetMapping("/callback/{provider}")
    public ResponseEntity<LoginToken> socialLogin(
            @PathVariable SocialProvider provider,
            @RequestParam("code") String code) {

        log.info("소셜 로그인: provider={}, code={}", provider, code);

        LoginToken loginToken = socialLoginService.login(provider, code);

        log.info("✅✅ACToken={}", loginToken.accessToken());
        System.out.println();
        log.info("✅✅RFToken={}", loginToken.refreshToken());

        return ResponseEntity.ok(loginToken);
    }

    // Apple의 경우 POST 방식 콜백 지원 아직 미정
    @PostMapping("/login/apple")
    public ResponseEntity<LoginToken> appleLogin(@RequestParam("code") String code) {
        log.info("Apple 로그인: code={}", code);
        LoginToken loginToken = socialLoginService.login(SocialProvider.APPLE, code);
        return ResponseEntity.ok(loginToken);
    }
}