package boombimapi.domain.clova.infrastructure.repository;

import static boombimapi.global.constant.AiAttemptTokenRedisConstant.*;

import boombimapi.domain.clova.vo.AiAttemptToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AiAttemptTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public void saveAiAttemptToken(
        AiAttemptToken aiAttemptToken,
        String memberId,
        Long memberPlaceId
    ) {
        redisTemplate.opsForHash().putAll(
            KEY_PREFIX_AI_ATTEMPT_META + aiAttemptToken.value(),
            Map.of(
                FIELD_MEMBER_ID, memberId,
                FIELD_MEMBER_PLACE_ID, String.valueOf(memberPlaceId),
                FIELD_CREATED_AT, String.valueOf(System.currentTimeMillis())
            )
        );
    }

    public void setActiveAiAttemptPointer(
        String memberId,
        AiAttemptToken aiAttemptToken
    ) {
        redisTemplate.opsForValue()
            .set(KEY_PREFIX_ACTIVE_POINTER + memberId, aiAttemptToken.value());
    }

    public Optional<Map<Object, Object>> getAiAttemptMeta(
        AiAttemptToken aiAttemptToken
    ) {
        Map<Object, Object> map = redisTemplate.opsForHash()
            .entries(KEY_PREFIX_AI_ATTEMPT_META + aiAttemptToken.value());

        if (map.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(map);
    }

    public Optional<AiAttemptToken> getActiveAiAttemptPointer(
        String memberId
    ) {
        String value = redisTemplate.opsForValue()
            .get(KEY_PREFIX_ACTIVE_POINTER + memberId);

        return Optional.of(new AiAttemptToken(value));
    }

    public boolean acquireOnce(
        AiAttemptToken aiAttemptToken
    ) {
        Boolean ok = redisTemplate.opsForValue()
            .setIfAbsent(KEY_PREFIX_USED_FLAG + aiAttemptToken.value(), "1");

        return Boolean.TRUE.equals(ok);
    }

    public void deleteAllKeysForAttempt(
        AiAttemptToken aiAttemptToken
    ) {
        redisTemplate.delete(KEY_PREFIX_USED_FLAG + aiAttemptToken.value());
        redisTemplate.delete(KEY_PREFIX_AI_ATTEMPT_META + aiAttemptToken.value());
    }

}
