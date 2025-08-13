package boombimapi.domain.place.official.query.dto.response;

import boombimapi.domain.place.shared.dto.Coordinate;

public record MapMarkerResponse(
    Long id,
    String name,
    Coordinate coordinate,
    Double distance
) {

}
