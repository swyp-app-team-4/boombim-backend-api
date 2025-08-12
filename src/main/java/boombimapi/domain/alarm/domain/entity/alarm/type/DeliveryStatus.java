package boombimapi.domain.alarm.domain.entity.alarm.type;

public enum DeliveryStatus {
    PENDING,   // 발송 대기
    SENT,      // 발송 성공
    FAILED,    // 발송 실패 (네트워크/FCM 오류 등)
    READ       // 사용자가 알림 확인
}
