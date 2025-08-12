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
    public Vote(User user, String posId, double latitude, double longitude, String posName, boolean isVoteActivate) {
        this.user = user;
        this.posId = posId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.posName = posName;
        this.isVoteActivate = isVoteActivate;
        this.endTime = Instant.now().plus(30, ChronoUnit.MINUTES); // 생성 시 30분 뒤
    }


    public void updateIsVoteActivate(boolean isVoteActivate) {
        this.isVoteActivate = false;
    }

}

//1. 지역 누르면 투표 생성 api  - 중복 검사인지 확인해야됨 타이머는 30분 이때 다른 사용자가 똑같은거하면 덮어쓰기 이해되지??
//2. 투표하기 api - 4가지 중 중복 안되게
//3. 투표리스트(중복 질문자수 api 몇명이 궁금), 내 질문 api 합쳐서 드리기
//4. 그리고 애초에 투표리스트는 사용자가 거리 500m 지역만 활성화 이것도 3번이랑 연관
//5. 투표 종료 api
//6. 알림 2개는 나중에`