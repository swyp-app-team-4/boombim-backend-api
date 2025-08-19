package boombimapi.domain.alarm.domain.entity.alarm;



import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;

import boombimapi.domain.member.domain.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_member_id", nullable = false)
    private Member sender;

    @OneToMany(mappedBy = "alarm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlarmRecipient> recipients = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmType type;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason; // 전송 실패 시 원인

    @Builder
    public Alarm(String title, String message, AlarmType type, Member sender) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.sender = sender;
        this.status = AlarmStatus.PENDING;
    }

    public void updateStatus(AlarmStatus status) {
        this.status = status;
        if (status == AlarmStatus.SENT) this.sentAt = LocalDateTime.now();
    }

    public void updateFailureReason(String failureReason) {
        this.status = AlarmStatus.FAILED;
        this.failureReason = failureReason;
    }
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(); // 한국시간
        }

    }
}