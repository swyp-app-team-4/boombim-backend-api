package boombimapi.domain.alarm.presentation.dto.req;

import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 내역 조회 요청")
public record GetAlarmHistoryRequest(
        @Schema(description = "알림 내역 조회 요청" , example = "ANDROID or IOS")
        String deviceType

) {

}