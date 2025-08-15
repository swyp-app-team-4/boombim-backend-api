package boombimapi.domain.place.dto.request;

import boombimapi.global.dto.Coordinate;

public record ViewportRequest(
    Coordinate topLeft,
    Coordinate bottomRight,
    Coordinate memberCoordinate
) {

}
