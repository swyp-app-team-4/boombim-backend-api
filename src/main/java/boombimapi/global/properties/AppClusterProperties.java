package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cluster.app")
public record AppClusterProperties(
    int refZ,
    int maxZoomAtRefZ,
    int baseCellPixel,
    double tileSize,
    int minClusterSize
) implements ClusterProperties {

    @Override
    public boolean invertedZoom() {
        return true;
    }

}
