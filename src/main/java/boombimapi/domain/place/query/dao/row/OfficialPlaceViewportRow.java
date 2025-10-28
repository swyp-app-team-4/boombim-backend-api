package boombimapi.domain.place.query.dao.row;

import java.time.LocalDateTime;

public record OfficialPlaceViewportRow(
    Long id,
    String name,
    String legalDong,
    String imageUrl,
    Double centroidLatitude,
    Double centroidLongitude,
    String congestionLevelName,
    String congestionMessage,
    LocalDateTime observedAt,
    Boolean isFavorite
) {

}
