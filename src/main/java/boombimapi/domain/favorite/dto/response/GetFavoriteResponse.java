package boombimapi.domain.favorite.dto.response;

import java.time.LocalDateTime;

public record GetFavoriteResponse(
    Long favoriteId,
    Long memberPlaceId,
    Double latitude,
    Double longitude,
    String memberPlaceName,
    String congestionLevelName,
    String congestionMessage,
    LocalDateTime observedAt
) {

    public static GetFavoriteResponse of(
        Long favoriteId,
        Long memberPlaceId,
        Double latitude,
        Double longitude,
        String memberPlaceName,
        String congestionLevelName,
        String congestionMessage,
        LocalDateTime observedAt
    ) {
        return new GetFavoriteResponse(
            favoriteId,
            memberPlaceId,
            latitude,
            longitude,
            memberPlaceName,
            congestionLevelName,
            congestionMessage,
            observedAt
        );
    }

}
