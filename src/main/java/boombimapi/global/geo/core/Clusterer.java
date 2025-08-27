package boombimapi.global.geo.core;

import java.util.List;

public interface Clusterer {

    List<ClusterMarker> cluster(
        List<ClusterPoint> clusterPoints,
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude,
        int zoomLevel
    );

}
