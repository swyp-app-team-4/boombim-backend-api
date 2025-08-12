package boombimapi.domain.vote.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vote")
@Slf4j
@RequiredArgsConstructor
public class VoteController {

    //1. 지역 누르면 투표 생성 api  - 중복 검사인지 확인해야됨 타이머는 30분 이때 다른 사용자가 똑같은거하면 덮어쓰기 이해되지?? 또한 위도 경도 맞게
//2. 투표하기 api - 4가지 중 중복 안되게
//3. 투표리스트(중복 질문자수 api 몇명이 궁금), 내 질문 api 합쳐서 드리기
//4. 그리고 애초에 투표리스트는 사용자가 거리 500m 지역만 활성화 이것도 3번이랑 연관
//5. 투표 종료 api
//6. 알림 2개는 나중에`
    @PostMapping
    public void registerVote() {

    }
}
