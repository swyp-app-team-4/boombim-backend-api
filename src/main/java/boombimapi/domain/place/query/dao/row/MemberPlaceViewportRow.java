package boombimapi.domain.place.query.dao.row;

import java.time.LocalDateTime;

public record MemberPlaceViewportRow(
    Long id,
    String name,
    Double latitude,
    Double longitude,
    String congestionLevelName,
    String congestionMessage,
    boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {

}
