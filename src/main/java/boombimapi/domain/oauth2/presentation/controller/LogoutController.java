package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.LogoutService;
import boombimapi.domain.oauth2.cookie.AuthCookieManager;
import boombimapi.domain.oauth2.cookie.vo.AuthCookie;
import boombimapi.global.response.BaseOKResponse;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static boombimapi.global.response.ResponseMessage.LOGOUT_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Auth", description = "인증 관련 API")
public class LogoutController {

    private final LogoutService logoutService;
    private final AuthCookieManager authCookieManager;

    @Operation(summary = "[APP] 로그아웃", description = "사용자를 로그아웃하고 토큰을 무효화합니다.")
    @PostMapping("/app/oauth2/logout")
    public ResponseEntity<BaseOKResponse<Void>> logout(
        @AuthenticationPrincipal String userId,
        @RequestBody LogoutRequest request
    ) {
        logoutService.logout(userId, request.refreshToken());

        return ResponseEntity.ok(
            BaseOKResponse.of(
                HttpStatus.OK,
                LOGOUT_SUCCESS));
    }

    @Operation(summary = "[WEB] 로그아웃", description = "사용자를 로그아웃하고 토큰을 무효화합니다.")
    @PostMapping("/web/oauth2/logout")
    public ResponseEntity<BaseResponse<Void>> logoutWeb(
        @AuthenticationPrincipal String memberId,
        HttpServletRequest request
    ) {
        String refreshToken = authCookieManager.extractRefreshToken(request);

        logoutService.logout(
            memberId,
            refreshToken
        );

        AuthCookie logoutCookie = authCookieManager.createLogoutCookie();

        return ResponseEntity.ok()
            .header("Set-Cookie", logoutCookie.refreshTokenCookie().toString())
            .body(BaseResponse.of(
                    HttpStatus.OK,
                    LOGOUT_SUCCESS
                )
            );
    }

    public record LogoutRequest(String refreshToken) {

    }
}