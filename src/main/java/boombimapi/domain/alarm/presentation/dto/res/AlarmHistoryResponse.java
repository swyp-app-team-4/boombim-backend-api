package boombimapi.domain.alarm.presentation.dto.res;

import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 내역 조회 응답")
public record AlarmHistoryResponse(

        @Schema(description = "알림 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "중요 공지사항")
        String title,

        @Schema(description = "내용", example = "앱 업데이트가 있습니다.")
        String message,

        @Schema(description = "타입", example = "ANNOUNCEMENT")
        AlarmType type,

        @Schema(description = "상태", example = "SENT")
        AlarmStatus status,


        @Schema(description = "생성 시간")
        LocalDateTime createdAt,

        @Schema(description = "전송 시간")
        LocalDateTime sentAt,

        @Schema(description = "실패 사유")
        String failureReason

) {
    public static AlarmHistoryResponse of(Alarm alarm) {
        return new AlarmHistoryResponse(
                alarm.getId(),
                alarm.getTitle(),
                alarm.getMessage(),
                alarm.getType(),
                alarm.getStatus(),
                alarm.getCreatedAt(),
                alarm.getSentAt(),
                alarm.getFailureReason()
        );
    }
}