package boombimapi.domain.place.entity;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import boombimapi.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "member_places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "memberPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "memberPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCongestion> memberCongestions;

    @Column(name = "uuid", length = 32, nullable = false, unique = true)
    private String uuid;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "address", length = 32, nullable = false)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder
    private MemberPlace(
        String uuid,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl
    ) {
        this.uuid = uuid;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    public static MemberPlace of(
        String uuid,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl
    ) {
        return MemberPlace.builder()
            .uuid(uuid)
            .name(name)
            .address(address)
            .latitude(latitude)
            .longitude(longitude)
            .imageUrl(imageUrl)
            .build();
    }

}
