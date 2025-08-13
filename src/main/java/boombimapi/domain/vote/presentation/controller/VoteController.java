package boombimapi.domain.vote.presentation.controller;

import boombimapi.domain.vote.application.service.VoteService;
import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteDeleteReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import boombimapi.domain.vote.presentation.dto.res.VoteListRes;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vote")
@Slf4j
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    //1. 지역 누르면 투표 생성 api  - 중복 검사인지 확인해야됨 타이머는 30분 이때 다른 사용자가 똑같은거하면 덮어쓰기 이해되지?? 또한 위도 경도 맞게
//2. 투표하기 api - 4가지 중 중복 안되게
//3. 투표리스트(중복 질문자수 api 몇명이 궁금), 내 질문 api 합쳐서 드리기
//4. 그리고 애초에 투표리스트는 사용자가 거리 500m 지역만 활성화 이것도 3번이랑 연관
//5. 투표 종료 api
//6. 알림 2개는 나중에
//7. 투표 종료하면 시간도 초기화 그리고 시간로직 따로 개발해야될듯..!
    @Operation(description = "투표 생성 api")
    @PostMapping
    public void registerVote(@AuthenticationPrincipal String userId, @Valid @RequestBody VoteRegisterReq req) {
        voteService.registerVote(userId, req);
    }


    @Operation(description = "투표하기 api")
    @PostMapping("/answer")
    public void answerVote(@AuthenticationPrincipal String userId, @Valid @RequestBody VoteAnswerReq req) {
        voteService.answerVote(userId, req);
    }

    @Operation(description = "투표 종료하기 api")
    @DeleteMapping
    public void deleteVote(@AuthenticationPrincipal String userId, @Valid @RequestBody VoteDeleteReq req) {
        voteService.deleteVote(userId, req);
    }


    @Operation(description = "투표 리스트(투표목록/내질문) api")
    @GetMapping
    public ResponseEntity<VoteListRes> listVote(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(voteService.listVote(userId));
    }


}
