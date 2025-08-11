package boombimapi.domain.alarm.application.service;


import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.user.domain.entity.User;

import java.util.concurrent.CompletableFuture;

/**
 * FCM(Firebase Cloud Messaging) 관련 서비스 인터페이스
 */
public interface FcmService {

    /**
     * FCM 토큰 등록
     *
     * @param userId 사용자 ID
     * @param token FCM 토큰
     * @param deviceType 디바이스 타입 (ANDROID, IOS, WEB)
     */
    void registerToken(User user, String token, DeviceType deviceType);

    /**
     * 단일 사용자에게 알림 전송
     *
     * @param userId 대상 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 성공 여부
     */
    boolean sendNotificationToUser(String userId, String title, String body);

    /**
     * 모든 사용자에게 알림 전송 (비동기)
     *
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 결과 (성공/실패 건수 포함)
     */
    CompletableFuture<AlarmSendResult> sendNotificationToAll(String title, String body);

    /**
     * 오래된 비활성 토큰 정리
     */
    void cleanupOldTokens();

    /**
     * 사용자의 활성 토큰 개수 조회
     *
     * @param userId 사용자 ID
     * @return 활성 토큰 개수
     */
    int getUserTokenCount(String userId);
}
