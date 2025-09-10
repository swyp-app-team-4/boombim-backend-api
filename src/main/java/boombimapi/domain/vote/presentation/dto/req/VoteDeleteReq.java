package boombimapi.domain.vote.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 질문 종료 요청 DTO")
public record VoteDeleteReq(

        @Schema(description = "투표 ID", example = "1")
        Long voteId
) {
}
