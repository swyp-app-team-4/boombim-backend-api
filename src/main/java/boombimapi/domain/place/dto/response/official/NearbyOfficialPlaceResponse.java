package boombimapi.domain.place.dto.response.official;

import boombimapi.domain.place.repository.projection.NearbyOfficialPlaceProjection;

public record NearbyOfficialPlaceResponse(
    Long officialPlaceId,
    String officialPlaceName,
    String imageUrl,
    String congestionLevelName,
    double distanceMeters
) {

    public static NearbyOfficialPlaceResponse from(
        NearbyOfficialPlaceProjection row
    ) {
        return new NearbyOfficialPlaceResponse(
            row.getId(),
            row.getName(),
            row.getImageUrl(),
            row.getCongestionLevelName(),
            row.getDistanceMeters()
        );
    }

}
