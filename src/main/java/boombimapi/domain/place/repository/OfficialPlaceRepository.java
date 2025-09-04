package boombimapi.domain.place.repository;

import boombimapi.domain.place.entity.OfficialPlace;
import boombimapi.domain.place.repository.projection.NearbyOfficialPlaceProjection;
import java.util.List;

import boombimapi.domain.search.presentation.dto.PlaceNameProjection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OfficialPlaceRepository extends JpaRepository<OfficialPlace, Long> {

    // TODO: JPQL/Native 없이 파생 메서드로 우선 구현
    List<OfficialPlace> findByCentroidLatitudeBetweenAndCentroidLongitudeBetween(
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    );

    // 연관 검색
    @Query("""
            select o.name as name
            from OfficialPlace o
            where lower(o.name) like lower(concat('%', ?1, '%'))
            order by lower(o.name) asc
        """)
    List<PlaceNameProjection> searchByName(String keyword, Pageable pageable);

    // 검색 결과
    List<OfficialPlace> findEntitiesByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query(value = """
        WITH latest AS (
          SELECT DISTINCT ON (oc.official_place_id)
                 oc.official_place_id, oc.congestion_level_id, oc.observed_at
          FROM official_congestions oc
          ORDER BY oc.official_place_id, oc.observed_at DESC
        ),
        filtered AS (
          SELECT l.official_place_id, l.observed_at,
                 cl.name AS levelName, cl.message AS levelMessage
          FROM latest l
          JOIN congestion_levels cl ON cl.id = l.congestion_level_id
          WHERE cl.name IN ('여유','보통')
        )
        SELECT p.id                  AS id,
               p.name                AS name,
               p.image_url           AS imageUrl,
               6371000 * 2 * ASIN(SQRT(
                 POWER(SIN(RADIANS(p.centroid_latitude  - :latitude ) / 2), 2) +
                 COS(RADIANS(:latitude)) * COS(RADIANS(p.centroid_latitude)) *
                 POWER(SIN(RADIANS(p.centroid_longitude - :longitude) / 2), 2)
               ))                    AS distanceMeters,
               f.levelName           AS congestionLevelName
        FROM official_places p
        JOIN filtered f ON f.official_place_id = p.id
        ORDER BY distanceMeters ASC, f.observed_at DESC, p.id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyOfficialPlaceProjection> findNearbyNonCrowdedHaversine(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("limit") int limit
    );


}
