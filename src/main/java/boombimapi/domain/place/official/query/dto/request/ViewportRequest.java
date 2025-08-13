package boombimapi.domain.place.official.query.dto.request;

import boombimapi.domain.place.shared.dto.Coordinate;

public record ViewportRequest(
    Coordinate topLeft,
    Coordinate bottomRight,
    Coordinate memberCoordinate
) {

}
