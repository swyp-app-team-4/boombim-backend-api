package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.generation")
public record ClovaGenerationProperties(
    double topP,
    int topK,
    int maxTokens,
    double temperature,
    double repetitionPenalty
) {

}
