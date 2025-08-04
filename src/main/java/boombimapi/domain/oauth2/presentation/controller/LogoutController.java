package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.LogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class LogoutController {

    private final LogoutService logoutService;

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 토큰을 무효화합니다.")
    @PostMapping("/api/oauth2/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        logoutService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    public record LogoutRequest(String refreshToken) {}
}