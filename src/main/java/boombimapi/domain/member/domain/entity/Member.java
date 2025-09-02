package boombimapi.domain.member.domain.entity;

import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
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
@Table(name = "member")
public class Member {
    @Id
    @Column(unique = true, nullable = false)
    private String id;


    // 1) 내가 "보낸" 알림들 (Alarm.sender)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alarm> sentAlarms = new ArrayList<>();

    // 2) 내가 "받는" 알림들 (조인 엔티티 AlarmRecipient.user)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlarmRecipient> alarmRecipients = new ArrayList<>();

    // 3) 내 디바이스 토큰들 (FcmToken.user)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FcmToken> fcmTokens = new ArrayList<>();

    // 4) 내 투표 목록들  주인
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votess = new ArrayList<>();

    // 5) 내 투표 목록들  부하
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteDuplication> voteDuplications = new ArrayList<>();

    // 6) 내 투표 답변들
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteAnswer> voteAnswers = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCongestion> memberCongestions = new ArrayList<>();

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // 관리자 비번 null이어도됨
    private String password;

    // false면 알림끔 true면 알림 감
    @Column(name = "alarm_flag", nullable = false)
    private boolean alarmFlag;

    // 첫 로그인일시에는 false 이후에는 계속 true
    @Column(name = "name_flag", nullable = false)
    private boolean nameFlag;

    @Builder
    public Member(String id, String email, String name, String profile,
                  SocialProvider socialProvider, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profile = profile;
        this.socialProvider = socialProvider;
        this.role = role;
        this.alarmFlag = true;
        this.nameFlag = false;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateProfile(String profile) {
        this.profile = profile;
    }

    public void updateIsActivateNameFlag() {
        this.nameFlag = true;
    }

    public void updateIsActivateAlarmFlag() {
        this.alarmFlag = true;
    }

    public void updateIsDeactivateAlarmFlag() {
        this.alarmFlag = false;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(); // 한국시간
        }

    }
}