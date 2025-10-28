package boombimapi.domain.place.query.api.dto.response.marker;

import boombimapi.domain.place.query.api.dto.MarkerType;
import boombimapi.global.vo.Coordinate;
import java.util.Map;

public record ViewportClusterMarkerResponse(
    MarkerType markerType,
    Coordinate coordinate,
    int clusterSize,
    Map<String, Integer> congestionLevelCounts
) implements ViewportMarkerResponse {

    public static ViewportClusterMarkerResponse of(
        Coordinate coordinate,
        int clusterSize,
        Map<String, Integer> congestionLevelCounts
    ) {
        return new ViewportClusterMarkerResponse(
            MarkerType.CLUSTER,
            coordinate,
            clusterSize,
            congestionLevelCounts
        );
    }

}
