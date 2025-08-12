package boombimapi.domain.alarm.domain.entity.alarm;

import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.DeliveryStatus;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class AlarmRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alarm_id", nullable = false)
    private Alarm alarm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType; // ANDROID / IOS

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING; // PENDING/SENT/FAILED/READ 등

    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AlarmRecipient(Alarm alarm, User user, DeviceType deviceType) {
        this.alarm = alarm;
        this.user = user;
        this.deviceType=deviceType;
    }

    public void markSent() { this.deliveryStatus = DeliveryStatus.SENT; this.sentAt = LocalDateTime.now(); }
    public void markFailed(String reason) { this.deliveryStatus = DeliveryStatus.FAILED; this.failureReason = reason; }

}
