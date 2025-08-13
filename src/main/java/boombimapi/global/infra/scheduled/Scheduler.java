package boombimapi.global.infra.scheduled;

import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.application.service.VoteService;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final FcmService fcmService;
    private final VoteRepository voteRepository;
    private final VoteService voteService;
    private final AlarmService alarmService;

    /**
     * 매일 새벽 3시에 오래된 FCM 토큰 정리
     */
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

    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L) // 1분마다, 앱 시작 30초 후 시작
    @Transactional
    public void sweepExpiredVotes() {

        List<Vote> expiredVotes = voteRepository.findByVoteStatusAndEndTimeLessThanEqual(VoteStatus.PROGRESS, Instant.now());

        int n = voteRepository.bulkCloseExpired(VoteStatus.PROGRESS, VoteStatus.END, Instant.now());
        // log.debug("Closed {} expired votes", n);

        // 자동 종료도 알림 있어야됨
        for (Vote expiredVote : expiredVotes) {
            List<User> userList = voteService.getUsers(expiredVote);

            alarmService.sendEndVoteAlarm(expiredVote, userList);

        }

    }

}
