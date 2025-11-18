package boombimapi.domain.place.query.dao.jdbc;

import static boombimapi.domain.place.query.dao.mapper.MemberPlaceRowMapper.*;

import boombimapi.domain.place.query.dao.MemberPlaceQueryDao;
import boombimapi.domain.place.query.dao.param.ViewportParam;
import boombimapi.domain.place.query.dao.row.MemberPlaceViewportRow;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberPlaceQueryJdbcDao implements MemberPlaceQueryDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<MemberPlaceViewportRow> findInViewPort(
        ViewportParam viewportParam
    ) {
        String sql = """
            SELECT
                mp.id,
                mp.name,
                mp.latitude,
                mp.longitude,
                cl.name           AS congestion_level_name,
                mc.congestion_message,
                mc.created_at     AS created_at,
                mc.expires_at     AS expires_at,
                CASE\s
                  WHEN :memberId IS NULL THEN FALSE
                  WHEN f.place_id IS NOT NULL THEN TRUE
                  ELSE FALSE
                END AS is_favorite
            FROM member_places mp
            LEFT JOIN LATERAL (
               SELECT mc.*
               FROM member_congestions mc
               WHERE mc.member_place_id = mp.id
               ORDER BY mc.created_at DESC
               LIMIT 1
            ) mc ON TRUE
            LEFT JOIN congestion_levels cl
              ON cl.id = mc.congestion_level_id
            LEFT JOIN favorites f
              ON f.member_id = :memberId
             AND f.place_id  = mp.id
             AND f.place_type = 'MEMBER_PLACE'
            WHERE mp.latitude  BETWEEN :minLatitude  AND :maxLatitude
              AND mp.longitude BETWEEN :minLongitude AND :maxLongitude
            """;

        Map<String, Object> params = Map.of(
            "memberId", viewportParam.memberId(),
            "minLatitude", viewportParam.minLatitude(),
            "maxLatitude", viewportParam.maxLatitude(),
            "minLongitude", viewportParam.minLongitude(),
            "maxLongitude", viewportParam.maxLongitude()
        );

        return jdbcTemplate.query(
            sql,
            params,
            MEMBER_PLACE_VIEWPORT
        );
    }
}
