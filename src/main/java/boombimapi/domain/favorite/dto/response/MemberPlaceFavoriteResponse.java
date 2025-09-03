package boombimapi.domain.favorite.dto.response;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.entity.PlaceType;
import java.time.LocalDateTime;

public record MemberPlaceFavoriteResponse(
    Long favoriteId,
    Long placeId,
    PlaceType placeType,
    String name,
    String imageUrl,
    String congestionLevelName,
    LocalDateTime observedAt,
    long todayUpdateCount
) implements FavoriteResponse {

    public static MemberPlaceFavoriteResponse of(
        Long favoriteId,
        MemberPlace memberPlace,
        String congestionLevelName,
        LocalDateTime observedAt,
        long todayUpdateCount
    ) {
        return new MemberPlaceFavoriteResponse(
            favoriteId,
            memberPlace.getId(),
            PlaceType.MEMBER_PLACE,
            memberPlace.getName(),
            memberPlace.getImageUrl(),
            congestionLevelName,
            observedAt,
            todayUpdateCount
        );
    }

}
