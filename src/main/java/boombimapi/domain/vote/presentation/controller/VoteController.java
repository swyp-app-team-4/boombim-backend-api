package boombimapi.domain.vote.presentation.controller;

import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;
import boombimapi.domain.vote.application.service.VoteService;
import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteDeleteReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import boombimapi.domain.vote.presentation.dto.res.VoteListRes;
import boombimapi.global.response.BaseOKResponse;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static boombimapi.global.response.ResponseMessage.*;

@RestController
@RequestMapping("/api/vote")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Vote", description = "투표 관련 API")
public class VoteController {

    private final VoteService voteService;

    @Operation(summary = "투표 생성", description = "새로운 투표를 생성합니다. 현재 위치가 반경 500m 이내여야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "투표 생성 성공"),
            @ApiResponse(responseCode = "403", description = "500m 반경 초과"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "409", description = "중복된 장소")
    })
    @PostMapping
    public ResponseEntity<BaseOKResponse<Void>> registerVote(@AuthenticationPrincipal String userId, @Valid @RequestBody VoteRegisterReq req) {
        voteService.registerVote(userId, req);

        return ResponseEntity.ok(
                BaseOKResponse.of(
                        HttpStatus.OK,
                        VOTE_SUCCESS));
    }

    @Operation(summary = "투표하기", description = "기존 투표에 투표합니다. 종료된 투표이거나 이미 투표한 경우 오류가 발생합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "투표 성공"),
            @ApiResponse(responseCode = "400", description = "종료된 투표"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 또는 투표"),
            @ApiResponse(responseCode = "409", description = "이미 투표함")
    })
    @PostMapping("/answer")
    public ResponseEntity<BaseOKResponse<Void>> answerVote(@AuthenticationPrincipal String userId, @Valid @RequestBody VoteAnswerReq req) {
        voteService.answerVote(userId, req);

        return ResponseEntity.ok(
                BaseOKResponse.of(
                        HttpStatus.OK,
                        VOTE_SUCCESS));
    }

    @Operation(summary = "투표 종료", description = "본인이 생성한 투표를 종료합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "투표 종료 성공"),
            @ApiResponse(responseCode = "403", description = "투표 종료 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 또는 투표")
    })
    @PatchMapping
    public ResponseEntity<BaseOKResponse<Void>> endVote(@AuthenticationPrincipal String userId, @Valid @RequestBody VoteDeleteReq req) {
        voteService.endVote(userId, req);

        return ResponseEntity.ok(
                BaseOKResponse.of(
                        HttpStatus.OK,
                        VOTE_SUCCESS));
    }

    @Operation(summary = "투표 목록 조회", description = "현재 위치 기준 반경 500m 이내 투표 목록과 내 질문 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    @GetMapping
    public ResponseEntity<VoteListRes> listVote(
            @AuthenticationPrincipal String userId,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        return ResponseEntity.ok(voteService.listVote(userId, latitude, longitude));
    }


}
