package boombimapi.domain.place.dto.response;

import boombimapi.global.dto.Coordinate;

public record ViewportResponse(
    Long id,
    String name,
    Coordinate coordinate,
    double distance,
    String congestionLevelName,
    String congestionMessage
) {

}
