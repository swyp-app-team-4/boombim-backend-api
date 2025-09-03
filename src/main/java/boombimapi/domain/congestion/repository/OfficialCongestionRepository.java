package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.OfficialCongestion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialCongestionRepository extends JpaRepository<OfficialCongestion, Long> {

    // TODO: JPQL/Native 없이 파생 메서드로 우선 구현
    Optional<OfficialCongestion> findTopByOfficialPlaceIdOrderByObservedAtDesc(
        Long officialPlaceId
    );

}
