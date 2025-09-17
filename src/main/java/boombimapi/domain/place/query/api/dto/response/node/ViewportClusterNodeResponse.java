package boombimapi.domain.place.query.api.dto.response.node;

import boombimapi.domain.place.query.api.dto.MarkerType;
import boombimapi.global.vo.Coordinate;
import java.util.Map;

public record ViewportClusterNodeResponse(
    MarkerType type,
    Coordinate coordinate,
    int clusterSize,
    Map<String, Integer> congestionLevelCounts
) implements ViewportNodeResponse {

    public static ViewportClusterNodeResponse of(
        Coordinate coordinate,
        int clusterSize,
        Map<String, Integer> congestionLevelCounts
    ) {
        return new ViewportClusterNodeResponse(
            MarkerType.CLUSTER,
            coordinate,
            clusterSize,
            congestionLevelCounts
        );
    }

}
