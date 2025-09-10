package boombimapi.global.properties;

public record AiAttemptTokenBucketProperties(
    int capacity,
    double refillPerSecond,
    int idleTtlSeconds,
    int defaultCost
) {

}
