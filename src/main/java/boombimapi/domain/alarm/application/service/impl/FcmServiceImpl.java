package boombimapi.domain.alarm.application.service.impl;


import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.domain.repository.AlarmRecipientRepository;
import boombimapi.domain.alarm.domain.repository.FcmTokenRepository;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FcmServiceImpl implements FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final AlarmRecipientRepository alarmRecipientRepository;

    /**
     * FCM 토큰 등록
     */
    @Override
    public void registerToken(User user, String token, DeviceType deviceType) {
        try {
            // 기존 토큰이 있는지 확인
            Optional<FcmToken> existingToken = fcmTokenRepository.findByUserIdAndToken(user.getId(), token);

            if (existingToken.isPresent()) {
                // 기존 토큰이 있으면 활성화 및 마지막 사용 시간 업데이트
                FcmToken fcmToken = existingToken.get();
                fcmToken.activate();
                log.info("기존 FCM 토큰 활성화: userId={}, deviceType={}", user.getId(), deviceType);
            } else {
                // 새로운 토큰 생성
                FcmToken fcmToken = FcmToken.builder()
                        .user(user)
                        .token(token)
                        .deviceType(deviceType)
                        .build();
                fcmTokenRepository.save(fcmToken);
                log.info("새 FCM 토큰 등록: userId={}, deviceType={}", user.getId(), deviceType);
            }

            // 해당 사용자의 다른 동일 디바이스 타입 토큰들을 비활성화 (선택사항)
            // fcmTokenRepository.deactivateOtherTokens(userId, token);

        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패: userId={}, error={}", user.getId(), e.getMessage());
            throw new BoombimException(ErrorCode.SERVER_UNTRACKED_ERROR, "FCM 토큰 등록에 실패했습니다.");
        }
    }

    /**
     * 단일 사용자에게 알림 전송 보류
     */
    @Override
    public boolean sendNotificationToUser(String userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.warn("사용자의 활성 FCM 토큰이 없음: userId={}", userId);
            return false;
        }

        boolean hasSuccess = false;
        for (FcmToken token : tokens) {
            try {
                sendSingleNotification(token.getToken(), title, body);
                token.updateLastUsedAt();
                hasSuccess = true;
            } catch (Exception e) {
                log.error("FCM 전송 실패: userId={}, token={}, error={}",
                        userId, token.getToken().substring(0, 10) + "...", e.getMessage());
                // 토큰이 무효화된 경우 비활성화
                if (isTokenInvalid(e)) {
                    token.deactivate();
                }
            }
        }

        return hasSuccess;
    }

    /**
     * 모든 사용자에게 알림 전송 (배치)
     */
    @Async
    @Override
    public CompletableFuture<AlarmSendResult> sendNotificationToAll(String title, String body, Alarm alarm) {
        List<FcmToken> allTokens = fcmTokenRepository.findAllActiveTokens();
        log.info("전체 알림 전송 시작: 총 {} 개의 토큰", allTokens.size());

        int successCount = 0;
        int failureCount = 0;
        List<String> invalidTokens = new ArrayList<>();


        // 저장할 수신 기록
        List<AlarmRecipient> recipientsToSave = new ArrayList<>();

        // 배치로 전송 (최대 500개씩)
        int batchSize = 500;
        for (int i = 0; i < allTokens.size(); i += batchSize) {
            List<FcmToken> batch = allTokens.subList(i, Math.min(i + batchSize, allTokens.size()));
            AlarmSendResult batchResult = sendBatchNotification(batch, title, body);

            successCount += batchResult.successCount();
            failureCount += batchResult.failureCount();
            invalidTokens.addAll(batchResult.invalidTokens());

            Set<String> invalidSet = new HashSet<>(batchResult.invalidTokens());

            for (FcmToken ft : batch) {
                AlarmRecipient ar = AlarmRecipient.builder()
                        .alarm(alarm)
                        .user(ft.getUser())              // User 연관
                        .deviceType(ft.getDeviceType())  // IOS/ANDROID/WEB
                        .build();

                if (invalidSet.contains(ft.getToken())) {
                    // 영구 실패
                    ar.markFailed("INVALID_OR_UNREGISTERED");
                } else {
                    // 성공
                    ar.markSent();
                }
                recipientsToSave.add(ar);
            }
            log.info("배치 전송 완료: {}/{} (성공: {}, 실패: {})",
                    i + batch.size(), allTokens.size(), batchResult.successCount(), batchResult.failureCount());
        }

        // 무효한 토큰들 비활성화
        deactivateInvalidTokens(invalidTokens);

        alarmRecipientRepository.saveAll(recipientsToSave);
        
        // 실패 토큰 원인은 서버 문제, 잘못된 형식, 유저가 앱 삭제 등 등 여러가지 이유가 있음 이건 주기적으로 삭제할거임 스케줄러
        AlarmSendResult result = new AlarmSendResult(successCount, failureCount, invalidTokens);
        log.info("전체 알림 전송 완료: 성공={}, 실패={}", successCount, failureCount);

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 배치 알림 전송
     */

    private AlarmSendResult sendBatchNotification(List<FcmToken> tokens, String title, String body) {
        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getToken)
                .toList();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setIcon("ic_notification")
                                .setColor("#FF6B35")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build())
                .addAllTokens(tokenStrings)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendMulticast(message);

            List<String> invalidTokens = new ArrayList<>();
            List<SendResponse> responses = response.getResponses();

            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (!sendResponse.isSuccessful()) {
                    String errorCode = String.valueOf(sendResponse.getException().getErrorCode());
                    if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                        invalidTokens.add(tokenStrings.get(i));
                    }
                }
            }

            return new AlarmSendResult(
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    invalidTokens
            );

        } catch (Exception e) {
            log.error("배치 알림 전송 실패: {}", e.getMessage());
            return new AlarmSendResult(0, tokens.size(), tokenStrings);
        }
    }

    /**
     * 단일 알림 전송
     */
    private void sendSingleNotification(String token, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setIcon("ic_notification")
                                .setColor("#FF6B35")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build())
                .setToken(token)
                .build();

        firebaseMessaging.send(message);
    }

    /**
     * 무효한 토큰들 비활성화
     */
    private void deactivateInvalidTokens(List<String> invalidTokens) {
        for (String token : invalidTokens) {
            fcmTokenRepository.findByToken(token).ifPresent(FcmToken::deactivate);
        }
        log.info("무효한 토큰 {} 개 비활성화 완료", invalidTokens.size());
    }

    /**
     * 토큰 무효화 여부 확인
     */
    private boolean isTokenInvalid(Exception e) {
        if (e instanceof FirebaseMessagingException) {
            FirebaseMessagingException fme = (FirebaseMessagingException) e;
            return "UNREGISTERED".equals(fme.getErrorCode()) ||
                    "INVALID_ARGUMENT".equals(fme.getErrorCode());
        }
        return false;
    }

    /**
     * 오래된 비활성 토큰 정리 (스케줄링으로 실행)
     */
    @Override
    public void cleanupOldTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        fcmTokenRepository.deleteInactiveTokensOlderThan(cutoffDate);
        log.info("30일 이상 된 비활성 토큰 정리 완료");
    }

    /**
     * 사용자 토큰 개수 조회
     */
    @Override
    public int getUserTokenCount(String userId) {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId).size();
    }
}

