package boombimapi.global.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseExpiringEntity extends BaseEntity {

    @Column(name = "expires_at", nullable = false, columnDefinition = "timestamptz")
    private LocalDateTime expiresAt;

    protected Duration ttl() {
        return Duration.ofHours(1);
    }

    @PrePersist
    protected void setExpiresIfMissing() {
        if (expiresAt != null) {
            return;
        }

        LocalDateTime baseTime = getCreatedAt();
        if (baseTime == null) {
            baseTime = LocalDateTime.now();
        }
        expiresAt = baseTime.plus(ttl());
    }

    protected void setExpiresAt(
        LocalDateTime expiresAt
    ) {
        this.expiresAt = expiresAt;
    }

}
