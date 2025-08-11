package boombimapi.domain.alarm.presentation.dto.res;

import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 전송 결과")
public record SendAlarmResponse(

        @Schema(description = "알림 ID", example = "1")
        Long alarmId,

        @Schema(description = "전송 상태", example = "SENT")
        AlarmStatus status,

        @Schema(description = "성공한 전송 건수", example = "150")
        int successCount,

        @Schema(description = "실패한 전송 건수", example = "5")
        int failureCount,

        @Schema(description = "총 대상자 수", example = "155")
        int totalTargets,

        @Schema(description = "전송 완료 시간")
        LocalDateTime completedAt

) {
    public static SendAlarmResponse of(Alarm alarm, int successCount, int failureCount, int totalTargets) {
        return new SendAlarmResponse(
                alarm.getId(),
                alarm.getStatus(),
                successCount,
                failureCount,
                totalTargets,
                alarm.getSentAt()
        );
    }
}