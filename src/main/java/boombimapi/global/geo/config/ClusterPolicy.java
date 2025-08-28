package boombimapi.global.geo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cluster")
public record ClusterPolicy(
    int zoomRef,
    int baseCellPixel,
    double mergeFactor,
    double tileSize
) {

}
