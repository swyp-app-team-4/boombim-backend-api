package boombimapi.domain.member.presentation.controller;

import boombimapi.domain.member.application.service.MemberService;
import boombimapi.domain.member.presentation.dto.member.req.NicknameReq;
import boombimapi.domain.member.presentation.dto.member.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.member.res.GetNicknameRes;
import boombimapi.domain.member.presentation.dto.member.res.MyPageVoteRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Member", description = "사용자 전용 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "닉네임 수정 API", description = "닉네임을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @PatchMapping
    public void updateNickname(@AuthenticationPrincipal String userId, @RequestBody NicknameReq req) {
        memberService.updateNickname(userId, req.name());
    }


    @Operation(summary = "첫 닉네임 수정 API", description = "처음 로그인 할 시 닉네임 수정 화면 뜨는지 안뜨는지 확인하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @GetMapping("/name")
    public ResponseEntity<GetNicknameRes> getNameFlag(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(memberService.getNameFlag(userId));
    }

    @Operation(summary = "마이페이지(1번 구간) 사용자 정보 조회 API", description = "사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @GetMapping
    public ResponseEntity<GetMemberRes> getMember(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(memberService.getMember(userId));
    }

    @Operation(summary = "마이페이지(3번 구간) 나의 투표 조회 API", description = "나의 투표를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @GetMapping("/my-answer")
    public ResponseEntity<List<MyPageVoteRes>> getMpVoteAnswer(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(memberService.getMyVoteAnswer(userId));
    }

    @Operation(summary = "마이페이지(4번 구간) 나의 질문 조회 API", description = "나의 질문을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @GetMapping("/my-question")
    public ResponseEntity<List<MyPageVoteRes>> getMpVote(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(memberService.getMyVoteQuestion(userId));
    }


}