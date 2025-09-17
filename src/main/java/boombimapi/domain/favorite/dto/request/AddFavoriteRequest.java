package boombimapi.domain.favorite.dto.request;

import boombimapi.domain.place.shared.type.PlaceType;

public record AddFavoriteRequest(
    PlaceType placeType,
    Long placeId
) {

}
