package boombimapi.global.infra.exception.error.response;

import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@JsonInclude(Include.NON_NULL)
public record ErrorResponse(
    int status,
    int code,
    String message,
    LocalDateTime time,

    // optional
    LocalDateTime retryAt
) {

    public static ErrorResponse of(
        BoombimException e
    ) {
        return ErrorResponse.builder()
            .status(e.getHttpStatusCode())
            .code(e.getErrorCode().getCode())
            .message(e.getMessage())
            .time(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(
        ErrorCode errorCode
    ) {
        return ErrorResponse.builder()
            .status(errorCode.getHttpCode())
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .time(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(
        ErrorCode errorCode,
        String customMessage
    ) {
        return ErrorResponse.builder()
            .status(errorCode.getHttpCode())
            .code(errorCode.getCode())
            .message(customMessage)
            .time(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(
        ErrorCode errorCode,
        LocalDateTime now,
        LocalDateTime retryAt
    ) {
        return ErrorResponse.builder()
            .status(errorCode.getHttpCode())
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .time(now)
            .retryAt(retryAt)
            .build();
    }

}