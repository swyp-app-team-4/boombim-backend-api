package boombimapi.global.infra.scheduled;

import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    public String endAlarmTitle(Vote vote) {
        return "📍 " + vote.getPosName() + " 투표가 종료되었습니다!";
    }


    public String endAlarmMessage(Vote vote) {
        List<VoteAnswer> voteAnswers = vote.getVoteAnswers();

        // 타입별 개수 집계
        Map<VoteAnswerType, Long> countMap = voteAnswers.stream()
                .collect(Collectors.groupingBy(VoteAnswer::getAnswerType, Collectors.counting()));

        // 0표 처리 (없으면 0으로 세팅)
        for (VoteAnswerType type : VoteAnswerType.values()) {
            countMap.putIfAbsent(type, 0L);
        }

        // 메시지 구성
        StringBuilder sb = new StringBuilder();
        sb.append("📊 [투표 결과 알림] 📊\n");
        sb.append("장소: ").append(vote.getPosName()).append("\n\n");

        sb.append("🟢 여유: ").append(countMap.get(VoteAnswerType.RELAXED)).append("표\n");
        sb.append("🔵 보통: ").append(countMap.get(VoteAnswerType.COMMONLY)).append("표\n");
        sb.append("🟡 약간 혼잡: ").append(countMap.get(VoteAnswerType.BUSY)).append("표\n");
        sb.append("🔴 혼잡: ").append(countMap.get(VoteAnswerType.CROWDED)).append("표\n");

        return sb.toString();

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


}
