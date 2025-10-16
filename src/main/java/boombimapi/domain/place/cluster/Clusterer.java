package boombimapi.domain.place.cluster;

import boombimapi.domain.place.cluster.vo.ClusterInput;
import boombimapi.domain.place.cluster.vo.ClusterResult;
import java.util.List;

public interface Clusterer {

    List<ClusterResult> cluster(
        List<ClusterInput> clusterInputs,
        int zoomLevel
    );

}
