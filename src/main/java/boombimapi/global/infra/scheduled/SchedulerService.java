package boombimapi.global.infra.scheduled;

import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {
    private final FcmService fcmService;
    private final VoteRepository voteRepository;
    private final VoteDuplicationRepository voteDuplicationRepository;
    private final VoteAnswerRepository voteAnswerRepository;
    private final AlarmService alarmService;

    // 매일 새벽 3시에 오래된 FCM 토큰 정리
    @Scheduled(cron = "0 0 3 * * *") // 매일 오전 3시
    public void cleanupOldFcmTokens() {
        log.info("오래된 FCM 토큰 정리 작업 시작");
        try {
            fcmService.cleanupOldTokens();
            log.info("FCM 토큰 정리 작업 완료");
        } catch (Exception e) {
            log.error("FCM 토큰 정리 작업 중 오류 발생", e);
        }
    }

    // 투표 종류 후 알림
    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L) // 1분마다, 앱 시작 30초 후 시작
    @Transactional
    public void sweepExpiredVotes() {
        // 자동
        List<Vote> autoVotes = voteRepository.findByVoteStatusAndEndTimeLessThanEqual(VoteStatus.PROGRESS, Instant.now());

        // 투포 시간 된거 종료로 바꾸기
        voteRepository.bulkCloseExpired(VoteStatus.PROGRESS, VoteStatus.END, Instant.now());
        // log.debug("Closed {} expired votes", n);

        // 자동 종료 알림
        for (Vote autoVote : autoVotes) {
            List<User> userList = getUsers(autoVote);
            alarmService.sendEndVoteAlarm(autoVote, userList);

        }

        // 수동
        List<Vote> passivityVotes = voteRepository.findByPassivityAlarmFlagTrue();

        // 수동 종료 알림
        for (Vote passivityVote : passivityVotes) {
            // false로 전환
            passivityVote.updatePassivityAlarmDeactivate();

            List<User> userList = getUsers(passivityVote);
            alarmService.sendEndVoteAlarm(passivityVote, userList);
        }
    }


    private List<User> getUsers(Vote vote) {
        // 종료 알람 넣기
        Set<User> userSet = new HashSet<>();

        // 1) 투표 생성자
        userSet.addAll(voteRepository.findUsersByVote(vote));

        // 2) 중복투표한 유저
        userSet.addAll(voteDuplicationRepository.findUsersByVote(vote));

        // 3) 답변한 유저
        userSet.addAll(voteAnswerRepository.findUsersByVote(vote));

        // 최종 리스트
        List<User> userList = new ArrayList<>(userSet);
        return userList;
    }

}
