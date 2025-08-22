package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.CongestionLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongestionLevelRepository extends JpaRepository<CongestionLevel, Integer> {

}
