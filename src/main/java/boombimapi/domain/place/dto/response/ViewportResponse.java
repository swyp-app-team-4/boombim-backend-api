package boombimapi.domain.place.dto.response;

import boombimapi.global.dto.Coordinate;

public record ViewportResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String imageUrl,
    Coordinate coordinate,
    double distance,
    String congestionLevelName,
    String congestionMessage
) {

    public static ViewportResponse of(
        Long officialPlaceId,
        String officialPlaceName,
        String imageUrl,
        Coordinate coordinate,
        double distance,
        String congestionLevelName,
        String congestionMessage
    ) {
        return new ViewportResponse(
            officialPlaceId,
            officialPlaceName,
            imageUrl,
            coordinate,
            distance,
            congestionLevelName,
            congestionMessage
        );
    }

}
