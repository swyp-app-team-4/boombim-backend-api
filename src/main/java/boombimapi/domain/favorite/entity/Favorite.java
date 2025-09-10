package boombimapi.domain.favorite.entity;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.place.entity.PlaceType;
import boombimapi.global.base.BaseEntity;
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

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false, length = 20)
    private PlaceType placeType;

    @Builder
    private Favorite(
        Member member,
        Long placeId,
        PlaceType placeType
    ) {
        this.member = member;
        this.placeId = placeId;
        this.placeType = placeType;
    }

    public static Favorite of(
        Member member,
        Long placeId,
        PlaceType placeType
    ) {
        return Favorite.builder()
            .member(member)
            .placeId(placeId)
            .placeType(placeType)
            .build();
    }

}
