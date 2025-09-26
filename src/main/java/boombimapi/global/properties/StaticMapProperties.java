package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "static-map")
public record StaticMapProperties(
    String baseUrl,
    String apiKeyId,
    String apiKey,
    int width,
    int height,
    int level,
    int scale,
    String markerStyle,
    double viewSizeRatio
) {

}
