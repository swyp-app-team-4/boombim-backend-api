package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.LogoutService;
import boombimapi.global.response.BaseOKResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static boombimapi.global.response.ResponseMessage.LOGOUT_SUCCESS;
import static boombimapi.global.response.ResponseMessage.VOTE_SUCCESS;


@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class LogoutController {

    private final LogoutService logoutService;

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 토큰을 무효화합니다.")
    @PostMapping("/api/oauth2/logout")
    public ResponseEntity<BaseOKResponse<Void>> logout(@AuthenticationPrincipal String userId,
                                                       @RequestBody LogoutRequest request) {
        logoutService.logout(userId,request.refreshToken());

        return ResponseEntity.ok(
                BaseOKResponse.of(
                        HttpStatus.OK,
                        LOGOUT_SUCCESS));
    }

    public record LogoutRequest(String refreshToken) {
    }
}