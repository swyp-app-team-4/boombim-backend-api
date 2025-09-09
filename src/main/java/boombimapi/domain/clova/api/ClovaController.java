package boombimapi.domain.clova.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.clova.application.ClovaService;
import boombimapi.domain.clova.dto.request.GenerateCongestionMessageRequest;
import boombimapi.domain.clova.dto.request.IssueAiAttemptTokenRequest;
import boombimapi.domain.clova.dto.response.GenerateCongestionMessageResponse;
import boombimapi.domain.clova.dto.response.IssueAiAttemptTokenResponse;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clova")
@Tag(name = "Clova", description = "NAVER CLOVA Studio 관련 API")
public class ClovaController {

    private final ClovaService clovaService;

    @Operation(summary = "혼잡도 메시지 자동 완성", description = "사용자 혼잡도 생성 시 혼잡도 메시지를 CLOVA Studio로 자동 완성시킵니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 혼잡도 메시지 생성 성공")
    })
    @PostMapping("/congestion-message")
    public ResponseEntity<BaseResponse<GenerateCongestionMessageResponse>> generateCongestionMessage(
        @AuthenticationPrincipal String memberId,
        @RequestBody GenerateCongestionMessageRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GENERATE_CONGESTION_MESSAGE_SUCCESS,
                clovaService.generateCongestionMessage(memberId, request)
            )
        );
    }

    @Operation(summary = "AI 생성 토큰 발급", description = "사용자 혼잡도 생성 시 1회만 가능하도록 제한하는 토큰을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "AI 생성 토큰 발급 성공"),
        @ApiResponse(responseCode = "406", description = "AI 생성 토큰이 존재하지 않습니다."),
        @ApiResponse(responseCode = "403", description = "AI 생성 토큰 소유자와 요청 사용자가 일치하지 않습니다."),
        @ApiResponse(responseCode = "422", description = "AI 생성 토큰의 장소와 요청 값이 다릅니다."),
        @ApiResponse(responseCode = "406", description = "활성 AI 생성 토큰이 없습니다."),
        @ApiResponse(responseCode = "409", description = "AI 생성 토큰이 이미 사용되었습니다."),
        @ApiResponse(responseCode = "424", description = "AI 생성 토큰이 이미 교체되었습니다.")
    })
    @PostMapping("/issue-ai-attempt-token")
    public ResponseEntity<BaseResponse<IssueAiAttemptTokenResponse>> issueAiAttemptToken(
        @AuthenticationPrincipal String memberId,
        @RequestBody IssueAiAttemptTokenRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                ISSUE_AI_ATTEMPT_TOKEN_SUCCESS,
                clovaService.issueAiAttemptToken(memberId, request)
            )
        );
    }

}
