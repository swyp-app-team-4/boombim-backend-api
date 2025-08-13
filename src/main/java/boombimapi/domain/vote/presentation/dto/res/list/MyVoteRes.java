package boombimapi.domain.vote.presentation.dto.res.list;

import boombimapi.domain.vote.domain.entity.type.VoteStatus;

import java.time.LocalDateTime;

public record MyVoteRes(
        // 투표 pk
        Long voteId,

        // 투표 중복자수 ~~ 몇명이 궁금해하고 있어요
        Long voteDuplicationCnt,

        // 생성날짜 몇분전 하기위해서
        LocalDateTime createdAt,

        // 장소 이름
        String posName,

        // 여유
        Long relaxedCnt,

        // 보통
        Long commonly,

        // 약간 붐빔
        Long slightlyBusyCnt,

        // 붐빔
        Long crowedCnt,

        // 전체 타입 즉 내 질문
        String allType,


        // 진행중인지 종료인지
        VoteStatus voteStatus


) {
    public static MyVoteRes of(Long voteId, Long voteDuplicationCnt, LocalDateTime createdAt, String posName, Long relaxedCnt,
                               Long commonly, Long slightlyBusyCnt, Long crowedCnt, String allType, VoteStatus voteStatus) {
        return new MyVoteRes(voteId, voteDuplicationCnt, createdAt, posName, relaxedCnt, commonly, slightlyBusyCnt, crowedCnt, allType, voteStatus);
    }
}
