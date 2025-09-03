package boombimapi.domain.congestion.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.congestion.application.MemberCongestionService;
import boombimapi.domain.congestion.dto.request.CreateMemberCongestionRequest;
import boombimapi.domain.congestion.dto.response.CreateMemberCongestionResponse;
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
@RequestMapping("/member-congestion")
@Tag(name = "Member Congestion", description = "사용자 혼잡도 관련 API")
public class MemberCongestionController {

    private final MemberCongestionService memberCongestionService;

    @Operation(summary = "사용자 혼잡도 생성", description = "사용자 장소에 새로운 사용자 혼잡도를 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 혼잡도 생성 성공")
    })
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<CreateMemberCongestionResponse>> createMemberCongestion(
        @AuthenticationPrincipal String memberId,
        @RequestBody CreateMemberCongestionRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                CREATE_MEMBER_CONGESTION_SUCCESS,
                memberCongestionService.createMemberCongestion(memberId, request)
            )
        );
    }


}
