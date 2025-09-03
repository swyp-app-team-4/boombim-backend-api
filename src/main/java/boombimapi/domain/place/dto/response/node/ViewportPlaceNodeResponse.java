package boombimapi.domain.place.dto.response.node;

import boombimapi.domain.place.dto.type.MarkerType;
import boombimapi.domain.place.entity.PlaceType;
import boombimapi.global.dto.Coordinate;

public record ViewportPlaceNodeResponse(
    MarkerType type,
    Long memberPlaceId,
    String name,
    PlaceType placeType,
    Coordinate coordinate,
    Double distance,
    String congestionLevelName,
    String congestionMessage,
    boolean isFavorite
) implements ViewportNodeResponse {

    public static ViewportPlaceNodeResponse of(
        Long memberPlaceId,
        String name,
        Coordinate coordinate,
        Double distance,
        String congestionLevelName,
        String congestionMessage,
        boolean isFavorite
    ) {
        return new ViewportPlaceNodeResponse(
            MarkerType.PLACE,
            memberPlaceId,
            name,
            PlaceType.MEMBER_PLACE,
            coordinate,
            distance,
            congestionLevelName,
            congestionMessage,
            isFavorite
        );
    }

}
