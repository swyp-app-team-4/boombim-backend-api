package boombimapi.global.infra.exception.error;

import lombok.Getter;

@Getter
public class RateLimitedException extends RuntimeException {

    private final ErrorCode errorCode;
    private final long retryAfterSeconds;

    public RateLimitedException(
        ErrorCode errorCode,
        long retryAfterSeconds
    ) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }

}
