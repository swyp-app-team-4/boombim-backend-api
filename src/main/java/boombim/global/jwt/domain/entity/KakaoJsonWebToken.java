package boombim.global.jwt.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash(value = "KakaoJsonWebToken")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoJsonWebToken {
    @Id
    private String userId;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime expiresIn;
}
