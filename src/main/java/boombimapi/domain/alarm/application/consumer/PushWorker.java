package boombimapi.domain.alarm.application.consumer;

import boombimapi.domain.alarm.application.messaging.NotifyMessage;
import boombimapi.domain.alarm.application.service.impl.FcmSender;
import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.alarm.domain.repository.AlarmRecipientRepository;
import boombimapi.domain.alarm.domain.repository.AlarmRepository;
import boombimapi.domain.alarm.domain.repository.FcmTokenRepository;
import boombimapi.domain.alarm.infra.messaging.PushProducer;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class PushWorker {

    private final AlarmRepository alarmRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final AlarmRecipientRepository alarmRecipientRepository;

    private final FcmSender fcmSender;
    private final PushProducer pushProducer;

    private static final int MAX_RETRY = 4; // 0,1,2,3,4 → 총 5회
    private static final int BATCH_SIZE = 500;

    @RabbitListener(queues = RabbitMQConfig.Q_PUSH_NOW)
    public void onPushNow(NotifyMessage msg) {
        Long alarmId = msg.getAlarmId();
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalStateException("Alarm not found: " + alarmId));

        try {
            // 상태 업데이트: SENDING
            alarm.updateStatus(AlarmStatus.SENDING);

            // 대상 토큰 조회 (네 기존 로직 그대로)
            List<FcmToken> allTokens = fcmTokenRepository.findAllActiveTokens();
            int total = allTokens.size();

            int totalSuccess = 0;
            int totalFailure = 0;
            List<String> invalidTokensAll = new ArrayList<>();
            List<AlarmRecipient> recipientsToSave = new ArrayList<>();

            for (int i = 0; i < total; i += BATCH_SIZE) {
                List<FcmToken> batch = allTokens.subList(i, Math.min(i + BATCH_SIZE, total));
                AlarmSendResult r = fcmSender.sendBatchNotification(batch, msg.getTitle(), msg.getBody());

                totalSuccess += r.successCount();
                totalFailure += r.failureCount();
                invalidTokensAll.addAll(r.invalidTokens());

                // 수신 기록(네 기존 코드 재사용)
                Set<String> invalidSet = new HashSet<>(r.invalidTokens());
                for (FcmToken ft : batch) {
                    AlarmRecipient ar = AlarmRecipient.builder()
                            .alarm(alarm)
                            .member(ft.getMember())
                            .deviceType(ft.getDeviceType())
                            .build();
                    if (invalidSet.contains(ft.getToken())) {
                        ar.markFailed("INVALID_OR_UNREGISTERED");
                    } else {
                        ar.markSent();
                    }
                    recipientsToSave.add(ar);
                }
            }

            // 무효 토큰 비활성화 (네 기존 메서드 사용)
            deactivateInvalidTokens(invalidTokensAll);

            alarmRecipientRepository.saveAll(recipientsToSave);

            // 상태 업데이트
            if (totalSuccess > 0 && totalFailure == 0) {
                alarm.updateStatus(AlarmStatus.SENT);
            } else if (totalSuccess > 0) {
                alarm.updateStatus(AlarmStatus.SENT);
            } else {
                alarm.updateFailureReason("모든 대상자에게 전송 실패");
            }

            log.info("푸시 전송 완료: alarmId={}, success={}, failure={}", alarmId, totalSuccess, totalFailure);

        } catch (Exception e) {
            log.error("푸시 전송 처리 실패: alarmId={}, retryCount={}, err={}",
                    alarmId, msg.getRetryCount(), e.getMessage(), e);

            // 재시도 판단: 일시적 오류라면 retry 큐로, 아니면 실패 처리
            int nextRetry = (msg.getRetryCount() == null ? 0 : msg.getRetryCount()) + 1;
            if (nextRetry <= MAX_RETRY) {
                long delay = computeBackoffMillis(nextRetry); // 지수 백오프
                pushProducer.publishRetry(NotifyMessage.builder()
                        .alarmId(alarmId)
                        .title(msg.getTitle())
                        .body(msg.getBody())
                        .retryCount(nextRetry)
                        .build(), delay);
                log.warn("재시도 예약: alarmId={}, nextRetry={}, delayMs={}", alarmId, nextRetry, delay);
            } else {
                alarm.updateFailureReason("재시도 초과: " + e.getMessage());
                log.error("최대 재시도 초과, 알림 실패 처리: alarmId={}", alarmId);
            }
        }
    }

    // 간단 지수 백오프 (1m → 5m → 15m → 30m → 30m 상한)
    private long computeBackoffMillis(int retry) {
        switch (retry) {
            case 1: return 1 * 60_000L;
            case 2: return 5 * 60_000L;
            case 3: return 15 * 60_000L;
            default: return 30 * 60_000L;
        }
    }

    // === 네 기존 로직에 있던 메서드 그대로 사용/이식 ===
    private void deactivateInvalidTokens(List<String> invalidTokens) {
        for (String token : invalidTokens) {
            fcmTokenRepository.findByToken(token).ifPresent(FcmToken::deactivate);
        }
        log.info("무효한 토큰 {} 개 비활성화 완료", invalidTokens.size());
    }
}
