package boombimapi.global.infra.exception.error;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        int code,
        String message,
        LocalDateTime time
) {
    public static ErrorResponse of(BoombimException exception) {
        return new ErrorResponse(
                exception.getHttpStatusCode(),
                exception.getErrorCode().getCode(),
                exception.getMessage(),

                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getHttpCode(),
                errorCode.getCode(),
                errorCode.getMessage(),

                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(
                errorCode.getHttpCode(),
                errorCode.getCode(),
                customMessage,

                LocalDateTime.now()
        );
    }
}