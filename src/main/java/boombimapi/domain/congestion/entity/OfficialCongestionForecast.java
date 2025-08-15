package boombimapi.domain.congestion.entity;

import boombimapi.domain.place.entity.OfficialPlace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(name = "official_congestion_forecasts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialCongestionForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "official_place_id", nullable = false)
    private OfficialPlace officialPlace;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    @Column(name = "forecast_time", nullable = false)
    private LocalDateTime forecastTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forecast_congestion_level_id", nullable = false)
    private CongestionLevel forecastCongestionLevel;

    @Column(name = "forecast_population_min")
    private Long forecastPopulationMin;

    @Column(name = "forecast_population_max")
    private Long forecastPopulationMax;

    @Builder
    private OfficialCongestionForecast(
        OfficialPlace officialPlace,
        LocalDateTime observedAt,
        LocalDateTime forecastTime,
        CongestionLevel forecastCongestionLevel,
        Long forecastPopulationMin,
        Long forecastPopulationMax
    ) {
        this.officialPlace = officialPlace;
        this.observedAt = observedAt;
        this.forecastTime = forecastTime;
        this.forecastCongestionLevel = forecastCongestionLevel;
        this.forecastPopulationMin = forecastPopulationMin;
        this.forecastPopulationMax = forecastPopulationMax;
    }

    public static OfficialCongestionForecast of(
        OfficialPlace officialPlace,
        LocalDateTime observedAt,
        LocalDateTime forecastTime,
        CongestionLevel forecastCongestionLevel,
        Long forecastPopulationMin,
        Long forecastPopulationMax
    ) {
        return OfficialCongestionForecast.builder()
            .officialPlace(officialPlace)
            .observedAt(observedAt)
            .forecastTime(forecastTime)
            .forecastCongestionLevel(forecastCongestionLevel)
            .forecastPopulationMin(forecastPopulationMin)
            .forecastPopulationMax(forecastPopulationMax)
            .build();
    }

}
