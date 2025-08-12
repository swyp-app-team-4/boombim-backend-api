package boombimapi.domain.place.official.query.dto.response;

import boombimapi.domain.place.official.entity.OfficialPlace;
import boombimapi.domain.place.official.query.dto.common.Coordinate;

public record MapMarkerResponse(
    Long id,
    String name,
    Coordinate coordinate
) {

    public static MapMarkerResponse fromEntity(
        OfficialPlace officialPlace
    ) {
        return new MapMarkerResponse(
            officialPlace.getId(),
            officialPlace.getName(),
            Coordinate.of(
                officialPlace.getCentroidLatitude(),
                officialPlace.getCentroidLongitude()
            )
        );
    }

}
