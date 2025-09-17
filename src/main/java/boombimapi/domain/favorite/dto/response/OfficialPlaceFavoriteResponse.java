package boombimapi.domain.favorite.dto.response;

import boombimapi.domain.place.command.entity.OfficialPlace;
import boombimapi.domain.place.shared.type.PlaceType;
import java.time.LocalDateTime;

public record OfficialPlaceFavoriteResponse(
    Long favoriteId,
    Long placeId,
    PlaceType placeType,
    String name,
    String imageUrl,
    String congestionLevelName,
    LocalDateTime observedAt,
    boolean updatedToday
) implements FavoriteResponse {

    public static OfficialPlaceFavoriteResponse of(
        Long favoriteId,
        OfficialPlace officialPlace,
        String congestionLevelName,
        LocalDateTime observedAt,
        boolean updatedToday
    ) {
        return new OfficialPlaceFavoriteResponse(
            favoriteId,
            officialPlace.getId(),
            PlaceType.OFFICIAL_PLACE,
            officialPlace.getName(),
            officialPlace.getImageUrl(),
            congestionLevelName,
            observedAt,
            updatedToday
        );
    }

}
