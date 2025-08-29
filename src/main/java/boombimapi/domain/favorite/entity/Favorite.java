package boombimapi.domain.favorite.entity;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.global.base.BaseEntity;
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
@Table(name = "favorites")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_place_id", nullable = false)
    private MemberPlace memberPlace;

    @Builder
    private Favorite(
        Member member,
        MemberPlace memberPlace
    ) {
        this.member = member;
        this.memberPlace = memberPlace;
    }

    public static Favorite of(
        Member member,
        MemberPlace memberPlace
    ) {
        return Favorite.builder()
            .member(member)
            .memberPlace(memberPlace)
            .build();
    }

}
