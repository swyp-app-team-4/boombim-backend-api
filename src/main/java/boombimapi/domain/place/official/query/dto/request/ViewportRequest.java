package boombimapi.domain.place.official.query.dto.request;

import boombimapi.domain.place.official.query.dto.common.Coordinate;

public record ViewportRequest(
    Coordinate topLeft,
    Coordinate bottomRight
) {

}
