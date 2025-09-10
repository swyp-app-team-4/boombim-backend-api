package boombimapi.domain.alarm.presentation.dto.res;

import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.alarm.domain.entity.alarm.type.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 조회")
public record HistoryResponse(

        @Schema(description = "사용자별 알림 id", example = "3")
        Long alarmReId,

        @Schema(description = "알림 제목", example = "붐빔 알림) 새로운 업데이트가 있습니다!")
        String title,


        @Schema(description = "알림 타입", example = "공지, 이벤트,붐빔 알림 등등")
        AlarmType alarmType,

        @Schema(description = "알림 상태", example = "읽음, 발송 성공 ==> 이렇게 2개가 뜨는데 발송 성공은 안읽었다는거임")
        DeliveryStatus deliveryStatus,

        @Schema(description = "알림 온 시간", example = "")
        LocalDateTime alarmTime


) {

    public static HistoryResponse of(Long alarmId, String title, AlarmType alarmType,
                                     DeliveryStatus deliveryStatus, LocalDateTime alarmTime) {
        return new HistoryResponse(alarmId, title, alarmType, deliveryStatus, alarmTime);
    }
}
