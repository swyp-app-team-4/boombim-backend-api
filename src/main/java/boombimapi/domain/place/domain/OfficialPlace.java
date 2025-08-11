package boombimapi.domain.place.domain;

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

    @Column(name = "poi_code", length = 20, nullable = false)
    private String poiCode;

    @Column(name = "centroid_latitude", nullable = false)
    private Double centroidLatitude;    // 중심 위도

    @Column(name = "centroid_longitude", nullable = false)
    private Double centroidLongitude;   // 중심 경도

    @Column(name = "polygon_coordinates", nullable = false, columnDefinition = "jsonb")
    private String polygonCoordinates;


    @Builder
    private OfficialPlace(
        String name,
        String poiCode,
        Double centroidLatitude,
        Double centroidLongitude,
        String polygonCoordinates
    ) {
        this.name = name;
        this.poiCode = poiCode;
        this.centroidLatitude = centroidLatitude;
        this.centroidLongitude = centroidLongitude;
        this.polygonCoordinates = polygonCoordinates;
    }

    public static OfficialPlace of(
        String name,
        String poiCode,
        Double centroidLatitude,
        Double centroidLongitude,
        String polygonCoordinates
    ) {
        return OfficialPlace.builder()
            .name(name)
            .poiCode(poiCode)
            .centroidLatitude(centroidLatitude)
            .centroidLongitude(centroidLongitude)
            .polygonCoordinates(polygonCoordinates)
            .build();
    }

}