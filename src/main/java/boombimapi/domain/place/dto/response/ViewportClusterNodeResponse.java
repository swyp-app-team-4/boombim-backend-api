package boombimapi.domain.place.dto.response;

import boombimapi.domain.place.dto.type.MarkerType;
import boombimapi.global.dto.Coordinate;

public record ViewportClusterNodeResponse(
    MarkerType type,
    Coordinate coordinate,
    int clusterSize
) {

    public static ViewportClusterNodeResponse of(
        Coordinate coordinate,
        int clusterSize
    ) {
        return new ViewportClusterNodeResponse(
            MarkerType.CLUSTER,
            coordinate,
            clusterSize
        );
    }

}
