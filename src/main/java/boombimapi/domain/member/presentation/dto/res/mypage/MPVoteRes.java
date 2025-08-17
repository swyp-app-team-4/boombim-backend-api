package boombimapi.domain.member.presentation.dto.res.mypage;

import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;

import java.time.LocalDateTime;

public record MPVoteRes(

        // 해당 투표 질문 id
        Long voteId,
        LocalDateTime day,
        String posName,

        // 없음도 있어서 String으로 함
        String popularStatus,

        Long popularCnt
) {
    public static MPVoteRes of(Long voteId, LocalDateTime day, String posName, String popularStatus, Long popularCnt) {
        return new MPVoteRes(voteId, day, posName, popularStatus, popularCnt);
    }
}

