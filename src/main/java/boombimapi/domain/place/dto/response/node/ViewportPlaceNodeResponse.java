package boombimapi.domain.place.dto.response.node;

import boombimapi.domain.place.dto.type.MarkerType;
import boombimapi.domain.place.entity.PlaceType;
import boombimapi.global.vo.Coordinate;
import java.time.LocalDateTime;

public record ViewportPlaceNodeResponse(
    MarkerType type,
    Long memberPlaceId,
    String name,
    PlaceType placeType,
    Coordinate coordinate,
    Double distance,
    String congestionLevelName,
    String congestionMessage,
    LocalDateTime createdAt,
    boolean isFavorite
) implements ViewportNodeResponse {

    public static ViewportPlaceNodeResponse of(
        Long memberPlaceId,
        String name,
        Coordinate coordinate,
        Double distance,
        String congestionLevelName,
        String congestionMessage,
        LocalDateTime createdAt,
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
            createdAt,
            isFavorite
        );
    }

}
