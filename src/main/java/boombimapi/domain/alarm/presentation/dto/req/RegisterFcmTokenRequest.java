package boombimapi.domain.alarm.presentation.dto.req;

import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FCM 토큰 등록 요청")
public record RegisterFcmTokenRequest(

        @Schema(description = "FCM 토큰", example = "dGhpc2lzYWZha2V0b2tlbg...")
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String token,

        @Schema(description = "디바이스 타입", example = "ANDROID")
        @NotNull(message = "디바이스 타입은 필수입니다")
        DeviceType deviceType

) {}