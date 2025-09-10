package boombimapi.domain.alarm.presentation.dto.req;

import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(description = "관리자 알림 전송 요청")
@Builder
public record SendAlarmRequest(

        @Schema(description = "알림 제목", example = "중요 공지사항")
        @NotBlank(message = "제목은 필수입니다")
        String title,

        @Schema(description = "알림 내용", example = "앱 업데이트가 있습니다.")
        @NotBlank(message = "내용은 필수입니다")
        String message,

        @Schema(description = "알림 타입", example = "ANNOUNCEMENT")
        @NotNull(message = "알림 타입은 필수입니다")
        AlarmType type


) {}