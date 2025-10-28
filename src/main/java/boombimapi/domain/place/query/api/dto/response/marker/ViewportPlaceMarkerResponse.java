package boombimapi.domain.place.query.api.dto.response.marker;

import boombimapi.domain.place.query.api.dto.MarkerType;
import boombimapi.domain.place.shared.type.PlaceType;
import boombimapi.global.vo.Coordinate;
import java.time.LocalDateTime;

public record ViewportPlaceMarkerResponse(
    MarkerType markerType,
    Long placeId,
    String name,
    PlaceType placeType,
    Coordinate coordinate,
    Double distance,
    String congestionLevelName,
    String congestionMessage,
    LocalDateTime createdAt,
    boolean isFavorite
) implements ViewportMarkerResponse {

    public static ViewportPlaceMarkerResponse of(
        Long placeId,
        String name,
        PlaceType placeType,
        Coordinate coordinate,
        Double distance,
        String congestionLevelName,
        String congestionMessage,
        LocalDateTime createdAt,
        boolean isFavorite
    ) {
        return new ViewportPlaceMarkerResponse(
            MarkerType.PLACE,
            placeId,
            name,
            placeType,
            coordinate,
            distance,
            congestionLevelName,
            congestionMessage,
            createdAt,
            isFavorite
        );
    }

}
