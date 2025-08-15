package boombimapi.domain.place.dto.response;

import boombimapi.domain.congestion.entity.OfficialCongestionForecast;
import java.time.LocalDateTime;

public record OfficialPlaceForecast(
    LocalDateTime forecastTime,
    String congestionLevelName,
    Long forecastPopulationMin,
    Long forecastPopulationMax
) {

    public static OfficialPlaceForecast from(
        OfficialCongestionForecast entity
    ) {
        return new OfficialPlaceForecast(
            entity.getForecastTime(),
            entity.getForecastCongestionLevel().getName(),
            entity.getForecastPopulationMin(),
            entity.getForecastPopulationMax()
        );
    }
}
