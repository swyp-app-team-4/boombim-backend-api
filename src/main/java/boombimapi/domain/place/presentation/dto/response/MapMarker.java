package boombimapi.domain.place.presentation.dto.response;

import boombimapi.domain.place.domain.entity.OfficialPlace;
import boombimapi.domain.place.presentation.dto.common.Coordinate;

public record MapMarker(
    Long id,
    String name,
    Coordinate coordinate
) {

    public static MapMarker fromEntity(
        OfficialPlace officialPlace
    ) {
        return new MapMarker(
            officialPlace.getId(),
            officialPlace.getName(),
            Coordinate.of(
                officialPlace.getCentroidLatitude(),
                officialPlace.getCentroidLongitude()
            )
        );
    }

}
