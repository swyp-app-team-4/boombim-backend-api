package boombimapi.domain.place.query.dao.jdbc;

import boombimapi.domain.place.query.dao.OfficialPlaceQueryDao;
import boombimapi.domain.place.query.dao.row.OfficialPlaceViewportRow;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialPlaceQueryJdbcDao implements OfficialPlaceQueryDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<OfficialPlaceViewportRow> findInViewport(
        String memberId,
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    ) {
        String sql = """
            SELECT
                op.id,
                op.name,
                op.legal_dong,
                op.image_url,
                op.centroid_latitude,
                op.centroid_longitude,
                cl.name    AS congestion_level_name,
                cl.message AS congestion_message,
                -- memberId가 없으면 false, 있으면 존재여부로 판정
                CASE 
                  WHEN :memberId IS NULL THEN FALSE
                  WHEN f.place_id IS NOT NULL THEN TRUE
                  ELSE FALSE
                END AS is_favorite
            FROM official_places op
            LEFT JOIN LATERAL (
               SELECT oc.*
               FROM official_congestions oc
               WHERE oc.official_place_id = op.id
               ORDER BY oc.observed_at DESC
               LIMIT 1
            ) oc ON TRUE
            LEFT JOIN congestion_level cl ON cl.id = oc.congestion_level_id
            LEFT JOIN favorites f
              ON f.member_id = :memberId
             AND f.place_id  = op.id
             AND f.place_type = 'OFFICIAL_PLACE'
             AND (f.is_deleted = FALSE OR f.is_deleted IS NULL)
            WHERE op.centroid_latitude  BETWEEN :minLat AND :maxLat
              AND op.centroid_longitude BETWEEN :minLng AND :maxLng
            """;

        Map<String, Object> params = Map.of(
            "memberId", memberId,
            "minLat", minLatitude,
            "maxLat", maxLatitude,
            "minLng", minLongitude,
            "maxLng", maxLongitude
        );

        return jdbcTemplate.query(sql, params, (rs, i) -> new OfficialPlaceViewportRow(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("legal_dong"),
            rs.getString("image_url"),
            rs.getDouble("centroid_latitude"),
            rs.getDouble("centroid_longitude"),
            rs.getString("congestion_level_name"),
            rs.getString("congestion_message"),
            rs.getBoolean("is_favorite")
        ));
    }
}
