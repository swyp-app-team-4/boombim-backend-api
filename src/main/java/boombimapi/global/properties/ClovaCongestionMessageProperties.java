package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.congestion-message")
public record ClovaCongestionMessageProperties(
    String baseUrl,
    String apiKey
) {

}
