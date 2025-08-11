package boombimapi.domain.congestion.domain.entity;

import boombimapi.domain.place.domain.entity.OfficialPlace;
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

@Entity
@Getter
@Table(name = "official_congestions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialCongestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "official_place_id", nullable = false)
    private OfficialPlace officialPlace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "congestion_level_id", nullable = false)
    private CongestionLevel congestionLevel;

    @Column(name = "population_min")
    private Long populationMin;

    @Column(name = "population_max")
    private Long populationMax;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    @Builder
    private OfficialCongestion(
        OfficialPlace officialPlace,
        CongestionLevel congestionLevel,
        Long populationMin,
        Long populationMax,
        LocalDateTime observedAt
    ) {
        this.officialPlace = officialPlace;
        this.congestionLevel = congestionLevel;
        this.populationMin = populationMin;
        this.populationMax = populationMax;
        this.observedAt = observedAt;
    }

    public static OfficialCongestion of(
        OfficialPlace officialPlace,
        CongestionLevel congestionLevel,
        Long populationMin,
        Long populationMax,
        LocalDateTime observedAt
    ) {
        return OfficialCongestion.builder()
            .officialPlace(officialPlace)
            .congestionLevel(congestionLevel)
            .populationMin(populationMin)
            .populationMax(populationMax)
            .observedAt(observedAt)
            .build();
    }

}
