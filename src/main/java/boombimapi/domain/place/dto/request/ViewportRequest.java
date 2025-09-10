package boombimapi.domain.place.dto.request;

import boombimapi.global.vo.Coordinate;

public record ViewportRequest(
    Coordinate topLeft,
    Coordinate bottomRight,
    Coordinate memberCoordinate,
    Integer zoomLevel
) {

}
