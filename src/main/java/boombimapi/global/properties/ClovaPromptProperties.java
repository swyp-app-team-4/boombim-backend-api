package boombimapi.global.properties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.prompts")
public record ClovaPromptProperties(
    String autoCompleteCongestionMessage
) {

    public String autoCompleteCongestionMessage() {
        if (autoCompleteCongestionMessage == null || autoCompleteCongestionMessage.isBlank()) {
            return "";
        }
        // 혹시 공백/개행 들어오면 제거
        String compact = autoCompleteCongestionMessage.replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(compact);
        return new String(decoded, StandardCharsets.UTF_8);
    }

}
