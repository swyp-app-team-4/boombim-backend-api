package boombimapi.domain.alarm.infra.scheduler;

import boombimapi.domain.alarm.application.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmScheduler {

    private final FcmService fcmService;

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
}