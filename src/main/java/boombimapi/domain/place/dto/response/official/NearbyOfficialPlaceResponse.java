package boombimapi.domain.place.dto.response.official;

import boombimapi.domain.place.repository.projection.NearbyOfficialPlaceProjection;
import java.time.LocalDateTime;

public record NearbyOfficialPlaceResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String legalDong,
    String imageUrl,
    String congestionLevelName,
    LocalDateTime observedAt,
    double distanceMeters
) {

    public static NearbyOfficialPlaceResponse from(
        NearbyOfficialPlaceProjection row
    ) {
        return new NearbyOfficialPlaceResponse(
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
