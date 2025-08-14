package boombimapi.domain.place.official.query.repository;

import static boombimapi.domain.place.official.entity.QOfficialPlace.officialPlace;

import boombimapi.domain.place.shared.dto.Coordinate;
import boombimapi.domain.place.shared.dto.PlaceInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 조회(Query) 전용 리포지토리
 */
@Repository
@RequiredArgsConstructor
public class OfficialPlaceQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PlaceInfo> findViewportOfficialPlaces(
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    ) {
        return queryFactory.select(
                Projections.constructor(
                    PlaceInfo.class,
                    officialPlace.id,
                    officialPlace.name,
                    Projections.constructor(
                        Coordinate.class,
                        officialPlace.centroidLatitude,
                        officialPlace.centroidLongitude
                    )
                )
            ).from(officialPlace)
            .where(
                officialPlace.centroidLatitude.between(minLatitude, maxLatitude)
                    .and(officialPlace.centroidLongitude.between(minLongitude, maxLongitude))
            )
            .fetch();
    }


}
