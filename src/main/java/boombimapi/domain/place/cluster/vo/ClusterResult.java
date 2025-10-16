package boombimapi.domain.place.cluster.vo;

import java.util.List;

public record ClusterResult(
    double latitude,
    double longitude,
    int count,
    List<Long> memberPlaceIds
) {

}
