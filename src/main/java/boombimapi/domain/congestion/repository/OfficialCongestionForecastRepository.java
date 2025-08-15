package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.OfficialCongestionForecast;
import boombimapi.domain.place.entity.OfficialPlace;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialCongestionForecastRepository extends JpaRepository<OfficialCongestionForecast, Long> {

    List<OfficialCongestionForecast> findByOfficialPlaceAndObservedAtOrderByForecastTimeAsc(
        OfficialPlace officialPlace,
        LocalDateTime observedAt
    );

}
