package boombimapi.domain.alarm.application.service.impl;


import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.domain.repository.AlarmRecipientRepository;
import boombimapi.domain.alarm.domain.repository.FcmTokenRepository;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.alarm.presentation.dto.req.AlarmSendDto;
import boombimapi.domain.alarm.presentation.dto.req.AlarmSendResDto;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
    public void registerToken(Member user, String token, DeviceType deviceType) {
        try {
            // 기존 토큰이 있는지 확인
            Optional<FcmToken> existingToken = fcmTokenRepository.findByMemberIdAndToken(user.getId(), token);

            if (existingToken.isPresent()) {
                // 기존 토큰이 있으면 활성화 및 마지막 사용 시간 업데이트
                FcmToken fcmToken = existingToken.get();
                fcmToken.activate();
                log.info("기존 FCM 토큰 활성화: userId={}, deviceType={}", user.getId(), deviceType);
            } else {
                // 새로운 토큰 생성
                FcmToken fcmToken = FcmToken.builder()
                        .member(user)
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
     * 모든 사용자에게 알림 전송 (배치)
     */

    @Override
    public AlarmSendResult sendNotificationToAll(String title, String body, Alarm alarm) {
        List<FcmToken> allTokens = fcmTokenRepository.findAllActiveTokens();
        log.info("전체 알림 전송 시작: 총 {} 개의 토큰", allTokens.size());


        AlarmSendDto dto = AlarmSendDto.builder()
                .allTokens(allTokens)
                .title(title)
                .body(body)
                .alarm(alarm)
                .build();

        AlarmSendResDto res = sendProxyBatch(dto);

        // 실패 토큰 원인은 서버 문제, 잘못된 형식, 유저가 앱 삭제 등 등 여러가지 이유가 있음 이건 주기적으로 삭제할거임 스케줄러
        AlarmSendResult result = new AlarmSendResult(res.successCount(), res.failureCount(), res.invalidTokens());
        log.info("전체 알림 전송 완료: 성공={}, 실패={}", res.successCount(), res.failureCount());

        return result;
    }


    @Override
    public AlarmSendResult sendNotificationToVote(String title, String body, Alarm alarm, List<Member> userList) {
        List<FcmToken> allTokens = fcmTokenRepository.findActiveTokensForUsers(userList);
        log.info("투표한 유저들 알림 전송 시작: 총 {} 개의 토큰", allTokens.size());


        AlarmSendDto dto = AlarmSendDto.builder()
                .allTokens(allTokens)
                .title(title)
                .body(body)
                .alarm(alarm)
                .build();

        AlarmSendResDto res = sendProxyBatch(dto);

        // 실패 토큰 원인은 서버 문제, 잘못된 형식, 유저가 앱 삭제 등 등 여러가지 이유가 있음 이건 주기적으로 삭제할거임 스케줄러
        AlarmSendResult result = new AlarmSendResult(res.successCount(), res.failureCount(), res.invalidTokens());
        log.info("전체 알림 전송 완료: 성공={}, 실패={}", res.successCount(), res.failureCount());
        return result;
    }


    private AlarmSendResDto sendProxyBatch(AlarmSendDto dto) {

        // 저장할 수신 기록
        List<AlarmRecipient> recipientsToSave = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        List<String> invalidTokens = new ArrayList<>();

        // 배치로 전송 (최대 500개씩)
        int batchSize = 500;
        for (int i = 0; i < dto.allTokens().size(); i += batchSize) {
            List<FcmToken> batch = dto.allTokens().subList(i, Math.min(i + batchSize, dto.allTokens().size()));
            AlarmSendResult batchResult = sendBatchNotification(batch, dto.title(), dto.body());

            successCount += batchResult.successCount();
            failureCount += batchResult.failureCount();
            invalidTokens.addAll(batchResult.invalidTokens());

            Set<String> invalidSet = new HashSet<>(batchResult.invalidTokens());

            for (FcmToken ft : batch) {
                AlarmRecipient ar = AlarmRecipient.builder()
                        .alarm(dto.alarm())
                        .member(ft.getMember())              // User 연관
                        .deviceType(ft.getDeviceType())  // IOS/ANDROID/WEB
                        .build();

                if (invalidSet.contains(ft.getToken())) {
                    // 영구 실패
                    log.info("실패로 와라");
                    ar.markFailed("INVALID_OR_UNREGISTERED");
                } else {
                    // 성공
                    ar.markSent();
                }
                recipientsToSave.add(ar);
            }
            log.info("배치 전송 완료: {}/{} (성공: {}, 실패: {})",
                    i + batch.size(), dto.allTokens().size(), batchResult.successCount(), batchResult.failureCount());
        }

        // 무효한 토큰들 비활성화
        deactivateInvalidTokens(invalidTokens);

        alarmRecipientRepository.saveAll(recipientsToSave);

        return AlarmSendResDto.of(successCount, failureCount, invalidTokens);
    }


    /**
     * 배치 알림 전송
     */

    private AlarmSendResult sendBatchNotification(List<FcmToken> tokens, String title, String body) {
        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getToken)
                .toList();

        log.info("=== FCM 개별 전송 시작 ===");
        log.info("전송할 토큰 수: {}", tokenStrings.size());

        int successCount = 0;
        int failureCount = 0;
        List<String> invalidTokens = new ArrayList<>();

        for (String token : tokenStrings) {
            try {
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

                String response = firebaseMessaging.send(message);
                log.info("개별 전송 성공: {}", response);
                successCount++;

            } catch (Exception e) {
                log.error("개별 전송 실패 - 토큰: {}, 오류: {}", token.substring(0, 20) + "...", e.getMessage());
                failureCount++;

                // 토큰 관련 오류인 경우 invalid 목록에 추가
                if (e.getMessage() != null) {
                    invalidTokens.add(token);
                }
            }
        }

        log.info("FCM 전송 완료 - 성공: {}, 실패: {}", successCount, failureCount);
        return new AlarmSendResult(successCount, failureCount, invalidTokens);
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

}

