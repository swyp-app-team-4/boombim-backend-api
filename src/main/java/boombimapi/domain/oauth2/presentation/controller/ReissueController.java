package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.ReissueService;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Token", description = "토큰 재발급 API")
public class ReissueController {

    private final ReissueService reissueService;

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/api/reissue")

    public ResponseEntity<LoginToken> reissue(@RequestBody ReissueRequest request) {
        LoginToken loginToken = reissueService.reissue(request.refreshToken());
        return ResponseEntity.ok(loginToken);
    }

    // 요청 DTO 추가
    public record ReissueRequest(
            String refreshToken) {
    }
}