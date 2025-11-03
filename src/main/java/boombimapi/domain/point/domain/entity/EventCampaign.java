package boombimapi.domain.point.domain.entity;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.point.domain.entity.type.EventCategory;
import boombimapi.domain.point.domain.entity.type.PointAction;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
@Table(name = "event_campaign")
public class EventCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "eventCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventLog> eventlogs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "event_category", nullable = false)
    @Comment("이벤트 타입 ex) EVENT_PARTICIPATION_TICKETE")
    private EventCategory eventCategory;

    @Comment("이벤트 시작일")
    @Column(name = "event_start_date", nullable = false)
    private LocalDateTime eventStartDate;

    @Comment("이벤트 종료일")
    @Column(name = "event_end_date", nullable = false)
    private LocalDateTime eventEndDate;

    @Comment("당첨자 발표일")
    @Column(name = "winner_announcement_date", nullable = false)
    private LocalDateTime winnerAnnouncementDate;


    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public EventCampaign(EventCategory eventCategory,
                         LocalDateTime eventStartDate, LocalDateTime eventEndDate,
                         LocalDateTime winnerAnnouncementDate) {
        this.eventCategory = eventCategory;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.winnerAnnouncementDate = winnerAnnouncementDate;
    }
}
