package boombimapi.domain.vote.presentation.dto.req;

import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;

public record VoteAnswerReq(

        Long voteId,
        VoteAnswerType voteAnswerType
) {
}
