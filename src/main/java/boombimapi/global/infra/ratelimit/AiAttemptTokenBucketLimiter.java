package boombimapi.global.infra.ratelimit;

import boombimapi.global.properties.AiAttemptTokenBucketProperties;
import boombimapi.global.vo.AiAttemptRateLimitDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAttemptTokenBucketLimiter {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final AiAttemptTokenBucketProperties properties;
    private final DefaultRedisScript<String> aiAttemptTokenBucketScript;

    public AiAttemptRateLimitDecision checkAndConsume(
        String memberId,
        int cost
    ) {

        final String tokenBucketKey = tokenBucketKey(memberId);

        try {
            String json = redisTemplate.execute(
                aiAttemptTokenBucketScript,
                List.of(tokenBucketKey),
                String.valueOf(properties.capacity()),
                String.valueOf(properties.refillPerSecond()),
                String.valueOf(cost),
                String.valueOf(properties.idleTtlSeconds())
            );

            if (json == null) {
                log.warn("[RateLimit] Redis returned null. Degrading to OPEN. memberId={}", memberId);
                return new AiAttemptRateLimitDecision(true, 0L, properties.capacity());
            }

            return objectMapper.readValue(json, AiAttemptRateLimitDecision.class);

        } catch (Exception e) {
            log.error("[RateLimit] Lua execute → parse failed → OPEN. memberId: {}, error: {}", memberId, e.toString());
            return new AiAttemptRateLimitDecision(true, 0L, properties.capacity());
        }

    }

    private String tokenBucketKey(
        String memberId
    ) {
        return "ai:token-bucket:" + memberId;
    }

}
