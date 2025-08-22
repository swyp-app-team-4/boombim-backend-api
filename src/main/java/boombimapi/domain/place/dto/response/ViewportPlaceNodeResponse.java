package boombimapi.domain.place.dto.response;

import boombimapi.domain.place.dto.type.MarkerType;
import boombimapi.global.dto.Coordinate;

public record ViewportPlaceNodeResponse(
    MarkerType type,
    Long memberPlaceId,
    String name,
    Coordinate coordinate,
    Double distance,
    String congestionLevelName,
    String congestionMessage
) implements ViewportNodeResponse {

    public static ViewportPlaceNodeResponse of(
        Long memberPlaceId,
        String name,
        Coordinate coordinate,
        Double distance,
        String congestionLevelName,
        String congestionMessage
    ) {
        return new ViewportPlaceNodeResponse(
            MarkerType.PLACE,
            memberPlaceId,
            name,
            coordinate,
            distance,
            congestionLevelName,
            congestionMessage
        );
    }

}
