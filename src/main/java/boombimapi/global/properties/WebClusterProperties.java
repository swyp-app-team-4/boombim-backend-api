package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cluster.web")
public record WebClusterProperties(
    int refZ,
    int maxZoomAtRefZ,
    int baseCellPixel,
    double tileSize,
    int minClusterSize
) implements ClusterProperties {

}
