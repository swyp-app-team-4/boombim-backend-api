package boombimapi.domain.clova.dto.request;

import boombimapi.global.properties.ClovaGenerationProperties;
import java.util.List;

public record ClovaChatRequest(
    List<ChatMessage> messages,
    double topP,
    int topK,
    int maxTokens,
    double temperature,
    double repetitionPenalty
) {

    public static ClovaChatRequest of(
        String systemPrompt,
        String userContent,
        ClovaGenerationProperties generationProperties
    ) {
        ChatMessage system = new ChatMessage("system", List.of(new ChatContent("text", systemPrompt)));
        ChatMessage user = new ChatMessage("user", List.of(new ChatContent("text", userContent)));

        return new ClovaChatRequest(
            List.of(system, user),
            generationProperties.topP(),
            generationProperties.topK(),
            generationProperties.maxTokens(),
            generationProperties.temperature(),
            generationProperties.repetitionPenalty()
        );
    }

}
