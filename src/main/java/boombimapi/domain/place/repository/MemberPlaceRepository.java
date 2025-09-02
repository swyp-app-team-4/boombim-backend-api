package boombimapi.domain.place.repository;

import boombimapi.domain.place.entity.MemberPlace;
import java.util.List;
import java.util.Optional;

import boombimapi.domain.search.presentation.dto.PlaceNameProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPlaceRepository extends JpaRepository<MemberPlace, Long> {

    Optional<MemberPlace> findByUuid(String uuid);

    List<MemberPlace> findByLatitudeBetweenAndLongitudeBetween(
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    );

    // 연관 검색
    List<PlaceNameProjection> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // 검색 결과
    List<MemberPlace> findEntitiesByNameContainingIgnoreCase(String keyword, Pageable pageable);



}
