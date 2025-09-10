package boombimapi.domain.favorite.dto.request;

import boombimapi.domain.place.entity.PlaceType;

public record AddFavoriteRequest(
    PlaceType placeType,
    Long placeId
) {

}
