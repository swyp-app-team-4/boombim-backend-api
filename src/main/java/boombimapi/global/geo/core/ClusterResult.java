package boombimapi.global.geo.core;

import java.util.List;

public record ClusterResult(
    double latitude,
    double longitude,
    int count,
    List<Long> memberPlaceIds
) {

}
