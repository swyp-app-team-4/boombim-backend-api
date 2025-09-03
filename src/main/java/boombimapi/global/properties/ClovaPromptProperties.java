package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.prompts")
public record ClovaPromptProperties(
    String autoCompleteCongestionMessage
) {

}
