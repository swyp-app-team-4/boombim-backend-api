package boombimapi.domain.alarm.domain.entity.fcm;

import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fcm_token",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "token"})
) // 같은 토큰 중복 될까봐 넣었음
@Getter
@NoArgsConstructor
public class FcmToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 매핑: FK 컬럼 user_id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1024) // TEXT보다 VARCHAR 권장(인덱스 용이)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private boolean isActive = true;

    @Builder
    public FcmToken(User user, String token, DeviceType deviceType) {
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateLastUsedAt() { this.lastUsedAt = LocalDateTime.now(); }
    public void deactivate() { this.isActive = false; }
    public void activate() { this.isActive = true; updateLastUsedAt(); }
}

