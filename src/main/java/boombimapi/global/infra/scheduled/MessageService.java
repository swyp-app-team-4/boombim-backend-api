package boombimapi.global.infra.scheduled;

import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {
    private final VoteAnswerRepository voteAnswerRepository;

    public String endAlarmTitle(Vote vote) {
        return "📍 " + vote.getPosName() + " 투표가 종료되었습니다!";
    }

    public String endVoteQuestionAlarmMessage(Vote vote) {
        return "📊 투표가 종료되었습니다! 마이페이지에서 결과를 확인하세요.";
    }

    public String endVoteAnswerAlarmMessage(Vote vote) {
        VoteAnswerType type = getTopVotedType(vote);
        return "📢 결과: " + type.getDisplayName() + " 🗳 마이페이지에서 확인하세요.";
    }


    public String dailyCommunityTitle() {
        return "💬 오늘의 소통방에 참여해보세요!";
    }

    public String dailyCommunityMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("🙌 서로의 의견을 나누고 소통해요!\n");
        sb.append("👉 지금 바로 참여해서 여러분의 생각을 공유해보세요!");
        return sb.toString();
    }

    private VoteAnswerType getTopVotedType(Vote vote) {
        List<VoteAnswerRepository.TypeCount> rows = voteAnswerRepository.countByType(vote.getId());

        Map<VoteAnswerType, Long> counts = new EnumMap<>(VoteAnswerType.class);
        for (VoteAnswerType t : VoteAnswerType.values()) counts.put(t, 0L);
        for (var r : rows) counts.put(r.getType(), r.getCnt());

        return Arrays.stream(VoteAnswerType.values())
                .max(Comparator.comparingLong(counts::get))
                .orElseThrow();

    }


}
