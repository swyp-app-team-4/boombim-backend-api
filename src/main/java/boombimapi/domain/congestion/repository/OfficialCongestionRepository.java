package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.OfficialCongestion;

import java.util.List;
import java.util.Optional;

import boombimapi.domain.place.entity.OfficialPlace;
import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OfficialCongestionRepository extends JpaRepository<OfficialCongestion, Long> {

    // TODO: JPQL/Native 없이 파생 메서드로 우선 구현
    Optional<OfficialCongestion> findTopByOfficialPlaceIdOrderByObservedAtDesc(
        Long officialPlaceId
    );


    @Query("""
        select oc
        from OfficialCongestion oc
        join fetch oc.congestionLevel cl
        where oc.officialPlace = :place
        order by oc.observedAt desc
        """)
    List<OfficialCongestion> findLatestByPlaceFetchLevel(@Param("place") OfficialPlace place, Pageable pageable);

}
