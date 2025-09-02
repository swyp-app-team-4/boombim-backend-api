package boombimapi.domain.place.repository;

import boombimapi.domain.place.entity.OfficialPlace;
import java.util.List;

import boombimapi.domain.search.presentation.dto.PlaceNameProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialPlaceRepository extends JpaRepository<OfficialPlace, Long> {

    // TODO: JPQL/Native 없이 파생 메서드로 우선 구현
    List<OfficialPlace> findByCentroidLatitudeBetweenAndCentroidLongitudeBetween(
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    );

    // 연관 검색
    List<PlaceNameProjection> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // 검색 결과
    List<OfficialPlace> findEntitiesByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
