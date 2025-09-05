package boombimapi.domain.place.dto.response.official;

import boombimapi.domain.place.repository.projection.NearbyOfficialPlaceProjection;
import java.time.LocalDateTime;

public record NearbyNonCongestedOfficialPlaceResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String legalDong,
    String imageUrl,
    String congestionLevelName,
    LocalDateTime observedAt,
    double distanceMeters
) {

    public static NearbyNonCongestedOfficialPlaceResponse from(
        NearbyOfficialPlaceProjection row
    ) {
        return new NearbyNonCongestedOfficialPlaceResponse(
            row.getId(),
            row.getName(),
            row.getLegalDong(),
            row.getImageUrl(),
            row.getCongestionLevelName(),
            row.getObservedAt(),
            row.getDistanceMeters()
        );
    }

}
