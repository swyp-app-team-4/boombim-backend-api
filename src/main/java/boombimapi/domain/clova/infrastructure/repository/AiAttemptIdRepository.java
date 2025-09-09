package boombimapi.domain.clova.infrastructure.repository;

import static boombimapi.global.constant.AiAttemptRedisConstant.*;

import boombimapi.domain.clova.vo.AiAttemptId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AiAttemptIdRepository {

    private final StringRedisTemplate redisTemplate;

    public void saveAiAttemptId(
        AiAttemptId aiAttemptId,
        String memberId,
        Long memberPlaceId
    ) {
        redisTemplate.opsForHash().putAll(
            KEY_PREFIX_AI_ATTEMPT_META + aiAttemptId.value(),
            Map.of(
                FIELD_MEMBER_ID, memberId,
                FIELD_MEMBER_PLACE_ID, String.valueOf(memberPlaceId),
                FIELD_CREATED_AT, String.valueOf(System.currentTimeMillis())
            )
        );
    }

    public void setActiveAiAttemptPointer(
        String memberId,
        AiAttemptId aiAttemptId
    ) {
        redisTemplate.opsForValue()
            .set(KEY_PREFIX_ACTIVE_POINTER + memberId, aiAttemptId.value());
    }

    public Optional<Map<Object, Object>> getAiAttemptMeta(
        AiAttemptId aiAttemptId
    ) {
        Map<Object, Object> map = redisTemplate.opsForHash()
            .entries(KEY_PREFIX_AI_ATTEMPT_META + aiAttemptId.value());

        if (map.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(map);
    }

    public Optional<AiAttemptId> getActiveAiAttemptPointer(
        String memberId
    ) {
        String value = redisTemplate.opsForValue()
            .get(KEY_PREFIX_ACTIVE_POINTER + memberId);

        return Optional.of(new AiAttemptId(value));
    }

    public boolean acquireOnce(
        AiAttemptId aiAttemptId
    ) {
        Boolean ok = redisTemplate.opsForValue()
            .setIfAbsent(KEY_PREFIX_USED_FLAG + aiAttemptId.value(), "1");

        return Boolean.TRUE.equals(ok);
    }

    public void deleteAllKeysForAttempt(
        AiAttemptId aiAttemptId
    ) {
        redisTemplate.delete(KEY_PREFIX_USED_FLAG + aiAttemptId.value());
        redisTemplate.delete(KEY_PREFIX_AI_ATTEMPT_META + aiAttemptId.value());
    }

}
