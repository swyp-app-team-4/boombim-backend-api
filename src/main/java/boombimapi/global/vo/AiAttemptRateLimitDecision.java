package boombimapi.global.vo;

public record AiAttemptRateLimitDecision(
    boolean allowed,
    long retryAfterMs,
    double tokensLeft
) {

    public long retryAfterSeconds() {
        return Math.max(1, retryAfterMs / 1000);
    }

}
