package boombimapi.domain.place.dto.response.official;

import boombimapi.domain.place.entity.PlaceType;
import java.time.LocalDateTime;
import java.util.List;

public record OfficialPlaceOverviewResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String legalDong,
    PlaceType placeType,
    String poiCode,
    String imageUrl,
    String congestionLevelName,
    String congestionMessage,
    LocalDateTime observedAt,
    Double centroidLatitude,
    Double centroidLongitude,
    String polygonCoordinates,
    List<OfficialPlaceDemographics> demographics,
    List<OfficialPlaceForecast> forecasts,
    boolean isFavorite
) {

    public static OfficialPlaceOverviewResponse of(
        Long officialPlaceId,
        String officialPlaceName,
        String legalDong,
        String poiCode,
        String imageUrl,
        String congestionLevelName,
        String congestionMessage,
        LocalDateTime observedAt,
        Double centroidLatitude,
        Double centroidLongitude,
        String polygonCoordinates,
        List<OfficialPlaceDemographics> demographics,
        List<OfficialPlaceForecast> forecasts,
        boolean isFavorite
    ) {
        return new OfficialPlaceOverviewResponse(
            officialPlaceId,
            officialPlaceName,
            legalDong,
            PlaceType.OFFICIAL_PLACE,
            poiCode,
            imageUrl,
            congestionLevelName,
            congestionMessage,
            observedAt,
            centroidLatitude,
            centroidLongitude,
            polygonCoordinates,
            demographics,
            forecasts,
            isFavorite
        );
    }

}
