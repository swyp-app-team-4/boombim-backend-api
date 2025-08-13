package boombimapi.domain.vote.presentation.dto.res.list;

import java.time.LocalDateTime;

public record VoteRes(

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


        String allType
        // 사진은 흠



) {
    public static VoteRes of(Long voteId, Long voteDuplicationCnt, LocalDateTime createdAt, String posName, Long relaxedCnt, Long commonly, Long slightlyBusyCnt, Long crowedCnt,String allType) {
        return new VoteRes(voteId, voteDuplicationCnt, createdAt, posName, relaxedCnt, commonly, slightlyBusyCnt, crowedCnt, allType);
    }
}
