package boombimapi.domain.favorite.dto.response;

import boombimapi.domain.place.entity.PlaceType;
import java.time.LocalDateTime;

public sealed interface FavoriteResponse
    permits MemberPlaceFavoriteResponse, OfficialPlaceFavoriteResponse {

    Long favoriteId();

    Long placeId();

    PlaceType placeType();

    String name();

    String imageUrl();

    String congestionLevelName();

    LocalDateTime observedAt();

}
