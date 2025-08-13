package boombimapi.domain.vote.application.service;

import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteDeleteReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import boombimapi.domain.vote.presentation.dto.res.VoteListRes;
import org.springframework.web.bind.annotation.RequestParam;

public interface VoteService {

    void registerVote(String userId, VoteRegisterReq req);

    void answerVote(String userId, VoteAnswerReq req);

    void deleteVote(String userId, VoteDeleteReq req);

    VoteListRes listVote(String userId, double latitude,
                         double longitude);


}
