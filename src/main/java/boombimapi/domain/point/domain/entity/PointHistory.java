package boombimapi.domain.point.domain.entity;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.point.domain.entity.type.PointAction;
import boombimapi.domain.point.domain.entity.type.PointCategory;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
@Table(name = "point_history")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_category", nullable = false)
    @Comment("포인트별 타입 ex) 혼잡도, 이벤트")
    private PointCategory pointCategory;


    @Enumerated(EnumType.STRING)
    @Column(name = "point_action", nullable = false)
    @Comment("포인트 액션 타입 ex) EARN, USE")
    private PointAction pointAction;

    @Column(name = "amount", nullable = false)
    @Comment("포인트 변동량")
    private Long amount;


    @Builder
    public PointHistory(Member member, Long amount, PointCategory pointCategory, PointAction pointAction) {
        this.member = member;
        this.amount = amount;
        this.pointCategory = pointCategory;
        this.pointAction = pointAction;
    }

}
