package boombimapi.domain.place.official.query.repository;

import boombimapi.domain.place.official.entity.OfficialPlace;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OfficialPlaceRepository extends JpaRepository<OfficialPlace, Long> {

    @Query("""
          select p
          from OfficialPlace p
          where p.centroidLatitude between :minLat and :maxLat
            and (
                  (:minLng <= :maxLng and p.centroidLongitude between :minLng and :maxLng)
               or (:minLng >  :maxLng and (p.centroidLongitude >= :minLng or p.centroidLongitude <= :maxLng))
            )
        """)
    List<OfficialPlace> findInViewport(
        @Param("minLat") double minLat,
        @Param("maxLat") double maxLat,
        @Param("minLng") double minLng,
        @Param("maxLng") double maxLng
    );

}
