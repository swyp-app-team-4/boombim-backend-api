package boombimapi.domain.place.dto.response.official;

import boombimapi.domain.congestion.repository.OfficialPlaceCongestionRankProjection;
import java.time.LocalDateTime;

public record CongestedOfficialPlaceResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String legalDong,
    String imageUrl,
    String congestionLevelName,
    Double densityPerM2,
    LocalDateTime observedAt
) {

    public static CongestedOfficialPlaceResponse from(
        OfficialPlaceCongestionRankProjection projection
    ) {
        return new CongestedOfficialPlaceResponse(
            projection.getOfficialPlaceId(),
            projection.getOfficialPlaceName(),
            projection.getLegalDong(),
            projection.getImageUrl(),
            projection.getCongestionLevelName(),
            projection.getDensityPerM2(),
            projection.getObservedAt()
        );
    }

}
