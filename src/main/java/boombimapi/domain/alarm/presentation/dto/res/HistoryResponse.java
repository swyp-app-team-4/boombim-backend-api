package boombimapi.domain.alarm.presentation.dto.res;

import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.alarm.domain.entity.alarm.type.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 조회")
public record HistoryResponse(
        @Schema(description = "알림 제목", example = "붐빔 알림) 새로운 업데이트가 있습니다!")
        String title,

        @Schema(description = "알림 내용", example = "해당 시스템은 ~~~~")
        String message,

        @Schema(description = "알림 타입", example = "공지, 이벤트,붐빔 알림 등등")
        AlarmType alarmType,

        @Schema(description = "알림 상태", example = "읽음, 발송 성공 ==> 이렇게 2개가 뜨는데 발송 성공은 안읽었다는거임")
        DeliveryStatus deliveryStatus


) {

    public static HistoryResponse of(String title, String message, AlarmType alarmType, DeliveryStatus deliveryStatus) {
        return new HistoryResponse(title, message, alarmType, deliveryStatus);
    }
}
