package boombimapi.domain.vote.domain.entity;

import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.time.LocalDateTime;
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

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 투표 상태 위에 위에 꺼랑 중복되긴 하는데 앱단에서 보기 편하게 하라구 넣음
    @Column(nullable = false)
    private VoteStatus voteStatus;
    // 투표 타이머 앤 고민좀 !!
    @Column(nullable = false)
    private Instant endTime; // 생성 시 now() + 30m

    // 초기에는 무조건 false 그다음 사용자가 투표 종류하기 버튼 누르면 true로 바뀌고 스케줄러 거치면서 false로 바뀜
    @Column(nullable = false)
    private boolean passivityAlarmFlag;


    @Builder
    public Vote(User user, String posId, double latitude, double longitude, String posName) {
        this.user = user;
        this.posId = posId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.posName = posName;
        this.isVoteActivate = true;
        this.endTime = Instant.now().plus(30, ChronoUnit.MINUTES); // 생성 시 30분 뒤
        this.voteStatus = VoteStatus.PROGRESS;
        this.passivityAlarmFlag = false;
    }


    public void updateIsVoteDeactivate() {
        this.isVoteActivate = false;
    }
    public void updateStatusDeactivate() {
        this.voteStatus = VoteStatus.END;
    }

    public void updatePassivityAlarmActivate(){
        this.passivityAlarmFlag = true;
    }

    public void updatePassivityAlarmDeactivate(){
        this.passivityAlarmFlag = false;
    }

}