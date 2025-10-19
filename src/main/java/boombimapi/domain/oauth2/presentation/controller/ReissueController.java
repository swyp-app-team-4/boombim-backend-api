package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.oauth2.application.service.ReissueService;
import boombimapi.domain.oauth2.cookie.AuthCookieManager;
import boombimapi.domain.oauth2.cookie.vo.AuthCookies;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import boombimapi.global.response.BaseResponse;
import boombimapi.global.response.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Token", description = "토큰 재발급 API")
public class ReissueController {

    private final ReissueService reissueService;
    private final AuthCookieManager authCookieManager;

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/app/reissue")
    public ResponseEntity<LoginToken> reissue(@RequestBody ReissueRequest request) {
        LoginToken loginToken = reissueService.reissue(request.refreshToken());
        return ResponseEntity.ok(loginToken);
    }

    @Operation(summary = "(Web) 토큰 재발급", description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급하여 쿠키에 담아줍니다.")
    @PostMapping("/web/reissue")
    public ResponseEntity<BaseResponse<Void>> reissueWeb(
        HttpServletRequest request
    ) {
        String extractedRefreshToken = authCookieManager.extractRefreshToken(request);

        LoginToken reissuedLoginTokens = reissueService.reissue(extractedRefreshToken);

        AuthCookies authCookies = authCookieManager.reissueAuthCookies(
            reissuedLoginTokens.accessToken(),
            reissuedLoginTokens.refreshToken()
        );

        return ResponseEntity.ok()
            .header("Set-Cookie", authCookies.accessTokenCookie().toString())
            .header("Set-Cookie", authCookies.refreshTokenCookie().toString())
            .body(BaseResponse.of(
                    HttpStatus.OK,
                    ResponseMessage.REISSUE_TOKENS_SUCCESS
                )
            );
    }

    // 요청 DTO 추가
    public record ReissueRequest(
        String refreshToken) {

    }
}