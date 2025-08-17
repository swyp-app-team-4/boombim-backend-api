package boombimapi.domain.member.presentation.dto.res.mypage;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 개별 투표 응답")
public record MPVoteRes(

        @Schema(description = "투표 ID", example = "101")
        Long voteId,

        @Schema(description = "투표 생성 시간", example = "2025-07-15T09:30:00")
        LocalDateTime day,

        @Schema(description = "장소 이름", example = "강남 교보문고")
        String posName,

        @Schema(description = "가장 많이 투표된 상태 (없음 포함)", example = "CROWED")
        String popularStatus,

        @Schema(description = "해당 상태의 투표 수", example = "18")
        Long popularCnt
) {
    public static MPVoteRes of(Long voteId, LocalDateTime day, String posName, String popularStatus, Long popularCnt) {
        return new MPVoteRes(voteId, day, posName, popularStatus, popularCnt);
    }
}
