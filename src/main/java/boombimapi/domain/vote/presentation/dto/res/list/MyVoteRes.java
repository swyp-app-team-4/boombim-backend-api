package boombimapi.domain.vote.presentation.dto.res.list;

import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 질문 리스트 응답 DTO")
public record MyVoteRes(

        @Schema(description = "투표 ID", example = "1")
        Long voteId,

        @Schema(description = "투표 중복자 수 (궁금해하는 사람 수)", example = "3")
        Long voteDuplicationCnt,

        @Schema(description = "투표 생성일시", example = "2025-08-13T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "장소 이름", example = "서울역")
        String posName,

        @Schema(description = "여유 투표 수", example = "5")
        Long relaxedCnt,

        @Schema(description = "보통 투표 수", example = "8")
        Long commonly,

        @Schema(description = "약간 붐빔 투표 수", example = "4")
        Long slightlyBusyCnt,

        @Schema(description = "붐빔 투표 수", example = "2")
        Long crowedCnt,

        @Schema(description = "전체 타입 (예: '내 질문')", example = "내 질문")
        String allType,

        @Schema(description = "투표 상태 (진행중 / 종료)", example = "PROGRESS or END")
        VoteStatus voteStatus

) {
    public static MyVoteRes of(Long voteId, Long voteDuplicationCnt, LocalDateTime createdAt, String posName, Long relaxedCnt,
                               Long commonly, Long slightlyBusyCnt, Long crowedCnt, String allType, VoteStatus voteStatus) {
        return new MyVoteRes(voteId, voteDuplicationCnt, createdAt, posName, relaxedCnt, commonly, slightlyBusyCnt, crowedCnt, allType, voteStatus);
    }
}
