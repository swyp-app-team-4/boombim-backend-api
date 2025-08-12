package boombimapi.domain.alarm.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 토큰 등록 응답")
public record RegisterFcmTokenResponse(

        @Schema(description = "등록 성공 여부", example = "true")
        boolean success,

        @Schema(description = "메시지", example = "FCM 토큰이 성공적으로 등록되었습니다.")
        String message

) {

    public static RegisterFcmTokenResponse failure(String message) {
        return new RegisterFcmTokenResponse(false, message);
    }

    public static RegisterFcmTokenResponse sucess(){
        return new RegisterFcmTokenResponse(true, "FCM 토큰이 성공적으로 등록되었습니다.");
    }
}