package boombimapi.domain.place.dto.response.official;

import java.util.List;

public record OfficialPlaceOverviewResponse(
    Long id,
    String name,
    String poiCode,
    Double centroidLatitude,
    Double centroidLongitude,
    String polygonCoordinates,
    List<OfficialPlaceDemographics> demographics,
    List<OfficialPlaceForecast> forecasts
) {

}
