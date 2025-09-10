package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.OfficialCongestion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfficialCongestionRepository extends JpaRepository<OfficialCongestion, Long> {

    // TODO: JPQL/Native 없이 파생 메서드로 우선 구현
    Optional<OfficialCongestion> findTopByOfficialPlaceIdOrderByObservedAtDesc(
        Long officialPlaceId
    );

    @Query(value = """
        WITH latest AS (
          SELECT DISTINCT ON (oc.official_place_id)
                 oc.official_place_id,
                 oc.congestion_level_id,
                 oc.density_per_m2,
                 oc.observed_at
          FROM official_congestions oc
          ORDER BY oc.official_place_id, oc.observed_at DESC
        ),
        joined AS (
          SELECT l.official_place_id,
                 l.density_per_m2,
                 l.observed_at,
                 cl.name AS level_name,
                 CASE cl.name
                   WHEN '붐빔' THEN 1
                   WHEN '약간 붐빔' THEN 2
                   WHEN '보통' THEN 3
                   WHEN '여유' THEN 4
                   ELSE 5
                 END AS level_priority
          FROM latest l
          JOIN congestion_levels cl ON cl.id = l.congestion_level_id
        )
        SELECT p.id                    AS officialPlaceId,
               p.name                  AS officialPlaceName,
               p.legal_dong            AS legalDong,
               p.image_url             AS imageUrl,
               j.level_name            AS congestionLevelName,
               j.density_per_m2        AS densityPerM2,
               j.observed_at           AS observedAt
        FROM joined j
        JOIN official_places p ON p.id = j.official_place_id
        ORDER BY j.level_priority ASC,
                 j.density_per_m2 DESC NULLS LAST,
                 p.id ASC
        LIMIT :limit
        """, nativeQuery = true)
    java.util.List<OfficialPlaceCongestionRankProjection> findTopCongestedOfficialPlace(@Param("limit") int limit);

}
