package boombimapi.domain.alarm.application.service;

import boombimapi.domain.alarm.presentation.dto.req.GetAlarmHistoryRequest;
import boombimapi.domain.alarm.presentation.dto.req.RegisterFcmTokenRequest;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.res.AlarmHistoryResponse;
import boombimapi.domain.alarm.presentation.dto.res.HistoryResponse;
import boombimapi.domain.alarm.presentation.dto.res.RegisterFcmTokenResponse;
import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 알림 관리 서비스 인터페이스
 */
public interface AlarmService {

    /**
     * 관리자가 알림 전송 (비동기)
     *
     * @param senderUserId 발송자(관리자) ID
     * @param request 알림 전송 요청 정보
     * @return 전송 결과
     */
    SendAlarmResponse sendAlarm(String senderUserId, SendAlarmRequest request);

    /**
     * FCM 토큰 등록
     *
     * @param userId 사용자 ID
     * @param request FCM 토큰 등록 요청
     * @return 등록 결과
     */
    RegisterFcmTokenResponse registerFcmToken(String userId, RegisterFcmTokenRequest request);

    /**
     * 알림 내역 조회 (관리자용)
     *
     * @param userId 조회자(관리자) ID
     * @param request 조회 조건
     * @return 페이징된 알림 내역
     */
    List<HistoryResponse> getAlarmHistory(String userId, GetAlarmHistoryRequest request);

    /**
     * 특정 알림 상세 조회
     *
     * @param userId 조회자 ID
     * @param alarmId 알림 ID
     * @return 알림 상세 정보
     */

}