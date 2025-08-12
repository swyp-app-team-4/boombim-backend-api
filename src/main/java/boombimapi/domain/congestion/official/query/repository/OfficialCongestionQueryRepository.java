package boombimapi.domain.congestion.official.query.repository;

import static boombimapi.domain.congestion.official.entity.QOfficialCongestion.officialCongestion;
import static boombimapi.domain.congestion.common.entity.QCongestionLevel.congestionLevel;
import static boombimapi.domain.place.domain.entity.QOfficialPlace.officialPlace;

import boombimapi.domain.congestion.official.query.dto.response.OfficialCongestionResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * QueryDSL JPA를 이용한 조회(Query) 전용 리포지토리
 */
@Repository
@RequiredArgsConstructor
public class OfficialCongestionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<OfficialCongestionResponse> findLatestOfficialCongestionByOfficialPlaceId(
        Long officialPlaceId
    ) {
        OfficialCongestionResponse officialCongestionResponse = queryFactory.select(
                Projections.constructor(
                    OfficialCongestionResponse.class,
                    officialCongestion.id,
                    officialPlace.id,
                    officialPlace.name,
                    congestionLevel.id,
                    congestionLevel.name,
                    congestionLevel.message,
                    officialCongestion.populationMin,
                    officialCongestion.populationMax,
                    officialCongestion.observedAt
                )
            )
            .from(officialCongestion)
            .join(officialCongestion.officialPlace, officialPlace)
            .join(officialCongestion.congestionLevel, congestionLevel)
            .where(officialPlace.id.eq(officialPlaceId))
            .orderBy(officialCongestion.observedAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(officialCongestionResponse);
    }

}
