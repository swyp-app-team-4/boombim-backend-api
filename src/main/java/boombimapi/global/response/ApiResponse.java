package boombimapi.global.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
    int code,
    String status,
    String message,
    T data
) {

    public static <T> ApiResponse<T> of(
        HttpStatus status,
        ResponseMessage message,
        T data
    ) {
        return new ApiResponse<>(
            status.value(),
            status.getReasonPhrase(),
            message.getMessage(),
            data
        );
    }

}
