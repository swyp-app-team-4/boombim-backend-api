package boombimapi.domain.place.repository;

import boombimapi.domain.place.entity.MemberPlace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPlaceRepository extends JpaRepository<MemberPlace, Long> {

    Optional<MemberPlace> findByUuid(String uuid);

    List<MemberPlace> findByLatitudeBetweenAndLongitudeBetween(
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    );
}
