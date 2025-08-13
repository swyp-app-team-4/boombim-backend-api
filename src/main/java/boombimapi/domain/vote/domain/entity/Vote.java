package boombimapi.domain.vote.domain.entity;

import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteDuplication> voteDuplications = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteAnswer> voteAnswers = new ArrayList<>();


    // 장소 id
    @Column(nullable = false)
    private String posId;

    // 위도
    @Column(nullable = false)
    private double latitude;

    // 경도
    @Column(nullable = false)
    private double longitude;

    // 장소 이름
    @Column(nullable = false)
    private String posName;

    // 투표 진행중 false면 종료
    @Column(nullable = false)
    private boolean isVoteActivate;

    // 투표 타이머 앤 고민좀 !!
    @Column(nullable = false)
    private Instant endTime; // 생성 시 now() + 30m


    @Builder
    public Vote(User user, String posId, double latitude, double longitude, String posName) {
        this.user = user;
        this.posId = posId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.posName = posName;
        this.isVoteActivate = true;
        this.endTime = Instant.now().plus(30, ChronoUnit.MINUTES); // 생성 시 30분 뒤
    }


    public void updateIsVoteDeactivate() {
        this.isVoteActivate = false;
    }

}