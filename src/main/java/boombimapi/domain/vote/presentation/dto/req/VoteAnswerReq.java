package boombimapi.domain.vote.presentation.dto.req;

import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 참여 DTO")
public record VoteAnswerReq(

        @Schema(description = "투표 ID", example = "1")
        Long voteId,

        @Schema(description = "투표 답변 유형 (RELAXED, COMMONLY, SLIGHTLY_BUSY, CROWDED)", example = "RELAXED")
        VoteAnswerType voteAnswerType
) {
}