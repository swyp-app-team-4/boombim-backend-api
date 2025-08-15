package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.OfficialCongestionForecast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialCongestionForecastRepository extends JpaRepository<OfficialCongestionForecast, Long> {

}
