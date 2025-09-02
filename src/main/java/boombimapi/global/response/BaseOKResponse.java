package boombimapi.global.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record BaseOKResponse<T>(
        int status,
        int code,
        String message,
        LocalDateTime time
) {

    public static <T> BaseOKResponse<T> of(
            HttpStatus status,
            ResponseMessage message
    ) {
        return new BaseOKResponse<>(
                status.value(),
                status.value(),
                message.getMessage(),
                LocalDateTime.now()
        );
    }

}
