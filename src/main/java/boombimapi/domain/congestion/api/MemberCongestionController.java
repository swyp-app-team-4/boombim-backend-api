package boombimapi.domain.congestion.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.congestion.application.MemberCongestionService;
import boombimapi.domain.congestion.dto.request.CreateMemberCongestionRequest;
import boombimapi.domain.congestion.dto.response.CreateMemberCongestionResponse;
import boombimapi.global.response.BaseResponse;
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
public class MemberCongestionController {

    private final MemberCongestionService memberCongestionService;

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
