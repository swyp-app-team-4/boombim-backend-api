package boombimapi.domain.vote.application.service;

import boombimapi.domain.vote.presentation.dto.req.RegisterReq;

public interface VoteService {

    void registerVote(String userId, RegisterReq req);
}
