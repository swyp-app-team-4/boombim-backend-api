package boombimapi.domain.place.entity;

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
@Table(name = "official_places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "poi_code", length = 20, nullable = false, unique = true)
    private String poiCode;

    @Column(name = "legalDong", nullable = false)
    private String legalDong;

    @Column(name = "area_m2", nullable = false)
    private Double areaM2;

    @Column(name = "centroid_latitude", nullable = false)
    private Double centroidLatitude;    // 중심 위도

    @Column(name = "centroid_longitude", nullable = false)
    private Double centroidLongitude;   // 중심 경도

    @Column(name = "polygon_coordinates", nullable = false, columnDefinition = "jsonb")
    private String polygonCoordinates;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder
    private OfficialPlace(
        String name,
        String poiCode,
        String legalDong,
        Double areaM2,
        Double centroidLatitude,
        Double centroidLongitude,
        String polygonCoordinates,
        String imageUrl
    ) {
        this.name = name;
        this.poiCode = poiCode;
        this.legalDong = legalDong;
        this.areaM2 = areaM2;
        this.centroidLatitude = centroidLatitude;
        this.centroidLongitude = centroidLongitude;
        this.polygonCoordinates = polygonCoordinates;
        this.imageUrl = imageUrl;
    }

    public static OfficialPlace of(
        String name,
        String poiCode,
        String legalDong,
        Double areaM2,
        Double centroidLatitude,
        Double centroidLongitude,
        String polygonCoordinates,
        String imageUrl
    ) {
        return OfficialPlace.builder()
            .name(name)
            .poiCode(poiCode)
            .legalDong(legalDong)
            .areaM2(areaM2)
            .centroidLatitude(centroidLatitude)
            .centroidLongitude(centroidLongitude)
            .polygonCoordinates(polygonCoordinates)
            .imageUrl(imageUrl)
            .build();
    }

}