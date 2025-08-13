package boombimapi.domain.vote.application.service;

import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;

public interface VoteService {

    void registerVote(String userId, VoteRegisterReq req);

    void answerVote(String userId, VoteAnswerReq req);


}
