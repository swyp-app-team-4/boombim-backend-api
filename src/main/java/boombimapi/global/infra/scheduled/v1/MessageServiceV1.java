package boombimapi.global.infra.scheduled.v1;

import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceV1 {
    private final VoteAnswerRepository voteAnswerRepository;


    public String dailyCommunityTitle() {
        return "\uD83D\uDC40 붐빔 정도를 알고 싶어하는 사람이 있어요";
    }

    public String dailyCommunityMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("지금 있는 곳의 혼잡도를 공유해보세요!");
        return sb.toString();
    }


}
