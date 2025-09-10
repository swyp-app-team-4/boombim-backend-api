package boombimapi.domain.region.domain.repository;

import boombimapi.domain.region.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findAllByRegionDate(LocalDate regionDate);
}
