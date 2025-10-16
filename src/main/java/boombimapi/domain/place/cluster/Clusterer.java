package boombimapi.domain.place.cluster;

import boombimapi.global.geo.core.ClusterInput;
import boombimapi.global.geo.core.ClusterResult;
import java.util.List;

public interface Clusterer {

    List<ClusterResult> cluster(
        List<ClusterInput> clusterInputs,
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude,
        int zoomLevel
    );

}
