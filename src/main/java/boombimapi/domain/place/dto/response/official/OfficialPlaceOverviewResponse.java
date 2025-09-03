package boombimapi.domain.place.dto.response.official;

import java.time.LocalDateTime;
import java.util.List;

public record OfficialPlaceOverviewResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String poiCode,
    String imageUrl,
    LocalDateTime observedAt,
    Double centroidLatitude,
    Double centroidLongitude,
    String polygonCoordinates,
    List<OfficialPlaceDemographics> demographics,
    List<OfficialPlaceForecast> forecasts
) {

    public static OfficialPlaceOverviewResponse of(
        Long officialPlaceId,
        String officialPlaceName,
        String poiCode,
        String imageUrl,
        LocalDateTime observedAt,
        Double centroidLatitude,
        Double centroidLongitude,
        String polygonCoordinates,
        List<OfficialPlaceDemographics> demographics,
        List<OfficialPlaceForecast> forecasts
    ) {
        return new OfficialPlaceOverviewResponse(
            officialPlaceId,
            officialPlaceName,
            poiCode,
            imageUrl,
            observedAt,
            centroidLatitude,
            centroidLongitude,
            polygonCoordinates,
            demographics,
            forecasts
        );
    }

}
