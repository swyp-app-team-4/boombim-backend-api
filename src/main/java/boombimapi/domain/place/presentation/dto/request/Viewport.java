package boombimapi.domain.place.presentation.dto.request;

import boombimapi.domain.place.presentation.dto.common.Coordinate;

public record Viewport(
    Coordinate topLeft,
    Coordinate bottomRight
) {

}
