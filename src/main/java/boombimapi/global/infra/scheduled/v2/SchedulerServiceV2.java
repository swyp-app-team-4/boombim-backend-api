package boombimapi.global.infra.scheduled.v2;

import boombimapi.domain.alarm.application.messaging.EndVoteMessage;
import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.alarm.infra.messaging.PushProducer;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.point.domain.entity.Point;
import boombimapi.domain.point.domain.repository.PointRepository;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import boombimapi.global.infra.scheduled.v1.MessageServiceV1;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchedulerServiceV2 {
private final FcmService fcmService;
private final AlarmService alarmService;
private final MessageServiceV2 messageService;
private final PointRepository pointRepository;

@Value("${admin.id}")
private String adminId;


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


// 매일 오후 4시에 혼잡도 알림
@Scheduled(cron = "0 0 16 * * *") // 매일 오후 4시
public void sendDailyNotification() {
    log.info("오후 4시 알림 작업 시작");
    try {
        // 관리자 아이디 나중에 바꾸게씅.!
        alarmService.sendAllAlarm(adminId, SendAlarmRequest
                                                   .builder()
                                                   .title(messageService.dailyCommunityTitle())
                                                   .message(messageService.dailyCommunityMessage())
                                                   .type(AlarmType.COMMUNICATION).build());
        log.info("오후 4시 알림 작업 완료");
    } catch (Exception e) {
        log.error("오후 4시 알림 작업 중 오류 발생", e);
    }
}

@Scheduled(cron = "0 0 0 * * *") // 매일 00시 실행
public void resetEventCountDaily() {
    List<Point> allPoints = pointRepository.findAll();
    for (Point point : allPoints) {
        if (point.getApplyEventCount() != 0) {
            point.initApplyEventCnt();
        }
    }
}


}
