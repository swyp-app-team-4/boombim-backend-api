package boombimapi.domain.place.entity;

import boombimapi.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member_places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 32, nullable = false, unique = true)
    private String uuid;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder
    private MemberPlace(
        String uuid,
        Double latitude,
        Double longitude
    ) {
        this.uuid = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static MemberPlace of(
        String uuid,
        Double latitude,
        Double longitude
    ) {
        return MemberPlace.builder()
            .uuid(uuid)
            .latitude(latitude)
            .longitude(longitude)
            .build();
    }

}
