package boombimapi.domain.alarm.domain.entity.alarm;



import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmType type;

    @Column(nullable = false)
    private String senderUserId; // 관리자 ID

    @Column(nullable = true)
    private String targetUserId; // 특정 사용자 대상일 때, null이면 전체 사용자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason; // 전송 실패 시 원인

    @Builder
    public Alarm(String title, String message, AlarmType type,
                 String senderUserId, String targetUserId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.senderUserId = senderUserId;
        this.targetUserId = targetUserId;
        this.status = AlarmStatus.PENDING;
    }

    public void updateStatus(AlarmStatus status) {
        this.status = status;
        if (status == AlarmStatus.SENT) {
            this.sentAt = LocalDateTime.now();
        }
    }

    public void updateFailureReason(String failureReason) {
        this.status = AlarmStatus.FAILED;
        this.failureReason = failureReason;
    }
}