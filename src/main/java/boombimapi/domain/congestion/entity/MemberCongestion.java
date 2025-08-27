package boombimapi.domain.congestion.entity;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.global.base.BaseExpiringEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member_congestions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCongestion extends BaseExpiringEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_place_id", nullable = false)
    private MemberPlace memberPlace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "congestion_level_id", nullable = false)
    private CongestionLevel congestionLevel;

    @Column(name = "congestion_message", length = 100, nullable = false)
    private String congestionMessage;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder
    private MemberCongestion(
        Member member,
        MemberPlace memberPlace,
        CongestionLevel congestionLevel,
        String congestionMessage,
        Double latitude,
        Double longitude
    ) {
        this.member = member;
        this.memberPlace = memberPlace;
        this.congestionLevel = congestionLevel;
        this.congestionMessage = congestionMessage;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static MemberCongestion of(
        Member member,
        MemberPlace memberPlace,
        CongestionLevel congestionLevel,
        String congestionMessage,
        Double latitude,
        Double longitude
    ) {
        return MemberCongestion.builder()
            .member(member)
            .memberPlace(memberPlace)
            .congestionLevel(congestionLevel)
            .congestionMessage(congestionMessage)
            .latitude(latitude)
            .longitude(longitude)
            .build();
    }
}
