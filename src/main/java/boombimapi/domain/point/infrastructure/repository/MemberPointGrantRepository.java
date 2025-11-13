package boombimapi.domain.point.infrastructure.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberPointGrantRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String RECENT_KEY_PREFIX = "point:recent:";
    private static final String DAILY_KEY_PREFIX = "point:daily:";

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    public void removeStaledKeys(
        String memberId,
        long currentMs,
        long windowMs
    ) {
        redisTemplate.opsForZSet().removeRangeByScore(
            RECENT_KEY_PREFIX + memberId,
            0,
            currentMs - windowMs
        );
    }

    public Set<String> rangeByScore(
        String memberId,
        long fromMs,
        long toMs
    ) {
        return redisTemplate.opsForZSet().rangeByScore(
            RECENT_KEY_PREFIX + memberId,
            fromMs,
            toMs
        );
    }

    public void addRecentCoordinate(
        String memberId,
        double latitude,
        double longitude,
        long currentMs
    ) {
        String key = RECENT_KEY_PREFIX + memberId;
        String memberCoordinate = String.format(
            Locale.ROOT,
            "%.15f,%.15f",
            latitude,
            longitude
        );

        redisTemplate.opsForZSet().add(key, memberCoordinate, currentMs);
        redisTemplate.expire(key, 2, TimeUnit.HOURS);
    }

    public int getTodayCount(
        String memberId
    ) {
        String key = todayKey(memberId);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public void incrementToday(
        String memberId
    ) {
        String key = todayKey(memberId);
        Long after = redisTemplate.opsForValue().increment(key);

        if (after == null) {
            return;
        }

        if (after == 1L) {
            redisTemplate.expire(key, 3, TimeUnit.DAYS);
        }

    }

    private String todayKey(
        String memberId
    ) {
        String date = LocalDate.now(KST).format(DAY_FORMAT);
        return DAILY_KEY_PREFIX + memberId + ":" + date;
    }

}
