package boombimapi.domain.place.dto.response;

import boombimapi.domain.place.entity.PlaceType;
import boombimapi.global.vo.Coordinate;

public record ViewportResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String legalDong,
    PlaceType placeType,
    String imageUrl,
    Coordinate coordinate,
    double distance,
    String congestionLevelName,
    String congestionMessage,
    boolean isFavorite
) {

    public static ViewportResponse of(
        Long officialPlaceId,
        String officialPlaceName,
        String legalDong,
        String imageUrl,
        Coordinate coordinate,
        double distance,
        String congestionLevelName,
        String congestionMessage,
        boolean isFavorite
    ) {
        return new ViewportResponse(
            officialPlaceId,
            officialPlaceName,
            legalDong,
            PlaceType.OFFICIAL_PLACE,
            imageUrl,
            coordinate,
            distance,
            congestionLevelName,
            congestionMessage,
            isFavorite
        );
    }

}
