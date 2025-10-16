package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cluster.web")
public record WebClusterProperties(
    int zMappingBase,
    int baseCellPixel,
    double tileSize
) {

}
