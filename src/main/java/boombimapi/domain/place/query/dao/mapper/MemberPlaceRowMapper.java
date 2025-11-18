package boombimapi.domain.place.query.dao.mapper;

import boombimapi.domain.place.query.dao.row.MemberPlaceViewportRow;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;

public final class MemberPlaceRowMapper {

    private MemberPlaceRowMapper() {
    }

    public static final RowMapper<MemberPlaceViewportRow> MEMBER_PLACE_VIEWPORT =
        (resultSet, rowNumber) -> {
            return new MemberPlaceViewportRow(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getDouble("latitude"),
                resultSet.getDouble("longitude"),
                resultSet.getString("congestion_level_name"),
                resultSet.getString("congestion_message"),
                resultSet.getBoolean("is_favorite"),
                resultSet.getObject("created_at", LocalDateTime.class),
                resultSet.getObject("expires_at", LocalDateTime.class)
            );
        };
}
