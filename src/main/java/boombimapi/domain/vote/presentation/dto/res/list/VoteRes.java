package boombimapi.domain.vote.presentation.dto.res.list;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "전체 투표 목록 응답 DTO(500M)")
public record VoteRes(

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

        @Schema(description = "전체 타입 (예: '투표하기')", example = "투표하기")
        String allType

) {
    public static VoteRes of(Long voteId, Long voteDuplicationCnt, LocalDateTime createdAt, String posName, Long relaxedCnt, Long commonly, Long slightlyBusyCnt, Long crowedCnt, String allType) {
        return new VoteRes(voteId, voteDuplicationCnt, createdAt, posName, relaxedCnt, commonly, slightlyBusyCnt, crowedCnt, allType);
    }
}
