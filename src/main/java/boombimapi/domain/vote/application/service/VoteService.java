package boombimapi.domain.vote.application.service;

import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteDeleteReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import boombimapi.domain.vote.presentation.dto.res.VoteListRes;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface VoteService {

    void registerVote(String userId, VoteRegisterReq req);

    void answerVote(String userId, VoteAnswerReq req);

    SendAlarmResponse endVote(String userId, VoteDeleteReq req);

    VoteListRes listVote(String userId, double latitude,
                         double longitude);

    List<User> getUsers(Vote vote);
}
