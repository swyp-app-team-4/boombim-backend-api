package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit.ai.token-bucket")
public record AiAttemptTokenBucketProperties(
    int capacity,
    double refillPerSecond,
    int idleTtlSeconds,
    int defaultCost
) {

}
