package boombimapi.global.jwt.domain.entity;

import boombimapi.domain.user.domain.entity.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "JsonWebToken", timeToLive = 1209600)
@Builder
@Getter
public class JsonWebToken {
    @Id
    private String refreshToken;

    private String providerId;

    private String email;

    private Role role;
}
