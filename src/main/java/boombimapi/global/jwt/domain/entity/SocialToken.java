package boombimapi.global.jwt.domain.entity;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash(value = "SocialToken")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialToken {

    @Id
    private String id; // userId + provider로 구성된 복합 키

    @Indexed
    private String userId;

    @Indexed
    private SocialProvider provider;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime expiresIn;

    public static String generateId(String userId, SocialProvider provider) {
        return userId + ":" + provider.getValue();
    }
}