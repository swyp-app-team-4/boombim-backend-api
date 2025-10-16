package boombimapi.domain.place.cluster;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cluster.web")
public record WebClusterPolicy(
    int zMappingBase,
    int baseCellPixel,
    double tileSize
) {

}
