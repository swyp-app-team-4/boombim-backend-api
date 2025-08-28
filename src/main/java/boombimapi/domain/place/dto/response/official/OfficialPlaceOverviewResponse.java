package boombimapi.domain.place.dto.response.official;

import java.time.LocalDateTime;
import java.util.List;

public record OfficialPlaceOverviewResponse(
    Long id,
    String name,
    String poiCode,
    LocalDateTime observedAt,
    Double centroidLatitude,
    Double centroidLongitude,
    String polygonCoordinates,
    List<OfficialPlaceDemographics> demographics,
    List<OfficialPlaceForecast> forecasts
) {

}
