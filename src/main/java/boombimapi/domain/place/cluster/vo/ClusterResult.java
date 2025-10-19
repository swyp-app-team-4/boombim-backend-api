package boombimapi.domain.place.cluster.vo;

import java.util.List;

public record ClusterResult(
    Cell cell,
    double centroidWorldPixelX,
    double centroidWorldPixelY,
    int count,
    List<Long> placeIds
) {

    public static ClusterResult of(
        Cell cell,
        double centroidWorldPixelX,
        double centroidWorldPixelY,
        int count,
        List<Long> placeIds
    ) {
        return new ClusterResult(
            cell,
            centroidWorldPixelX,
            centroidWorldPixelY,
            count,
            placeIds
        );
    }

}
