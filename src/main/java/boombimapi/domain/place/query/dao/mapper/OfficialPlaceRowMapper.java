package boombimapi.domain.place.query.dao.mapper;

import boombimapi.domain.place.query.dao.row.OfficialPlaceViewportRow;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;

public final class OfficialPlaceRowMapper {

    private OfficialPlaceRowMapper() {
    }

    public static final RowMapper<OfficialPlaceViewportRow> OFFICIAL_PLACE_VIEWPORT = (resultSet, i) ->
        new OfficialPlaceViewportRow(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("legal_dong"),
            resultSet.getString("image_url"),
            resultSet.getDouble("centroid_latitude"),
            resultSet.getDouble("centroid_longitude"),
            resultSet.getString("congestion_level_name"),
            resultSet.getString("congestion_message"),
            resultSet.getObject("observed_at", LocalDateTime.class),
            resultSet.getBoolean("is_favorite")
        );

}
