package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.entity.OfficialCongestionDemographics;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialCongestionDemographicsRepository extends JpaRepository<OfficialCongestionDemographics, Long> {

    List<OfficialCongestionDemographics> findByOfficialCongestion(OfficialCongestion officialCongestion);

}
