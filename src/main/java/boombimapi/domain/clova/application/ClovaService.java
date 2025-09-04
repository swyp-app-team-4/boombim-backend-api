package boombimapi.domain.clova.application;

import boombimapi.domain.clova.dto.request.ClovaChatRequest;
import boombimapi.domain.clova.dto.request.GenerateCongestionMessageRequest;
import boombimapi.domain.clova.dto.response.ClovaResponse;
import boombimapi.domain.clova.dto.response.GenerateCongestionMessageResponse;
import boombimapi.domain.clova.infrastructure.ClovaCongestionMessageClient;
import boombimapi.domain.clova.infrastructure.parser.ClovaParser;
import boombimapi.global.properties.ClovaGenerationProperties;
import boombimapi.global.properties.ClovaPromptProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaService {

    private final ClovaParser parser;
    private final ClovaPromptProperties promptProperties;
    private final ClovaGenerationProperties clovaGenerationProperties;
    private final ClovaCongestionMessageClient clovaCongestionMessageClient;

    public GenerateCongestionMessageResponse generateCongestionMessage(
        GenerateCongestionMessageRequest request
    ) {
        String memberPlaceName = request.memberPlaceName();
        String congestionLevelName = request.congestionLevelName();
        String congestionMessage = request.congestionMessage();

        String systemPrompt = promptProperties.autoCompleteCongestionMessage();

        String userContent = buildUserContent(
            memberPlaceName,
            congestionLevelName,
            congestionMessage
        );

        ClovaChatRequest clovaChatRequest = ClovaChatRequest.of(
            systemPrompt,
            userContent,
            clovaGenerationProperties
        );

        try {
            ClovaResponse clovaResponse = clovaCongestionMessageClient
                .autoCompleteCongestionMessage(clovaChatRequest)
                .block(Duration.ofSeconds(30));

            String text = parser.toText(clovaResponse);

            if (text == null || text.isBlank()) {
                String fallback = fallback(memberPlaceName, congestionLevelName);
                return GenerateCongestionMessageResponse.from(fallback);
            }
            return GenerateCongestionMessageResponse.from(text);
        } catch (Exception e) {
            String fallback = fallback(memberPlaceName, congestionLevelName);
            return GenerateCongestionMessageResponse.from(fallback);
        }

    }

    private String buildUserContent(
        String memberPlaceName,
        String congestionLevelName,
        String congestionMessage
    ) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("장소명: ").append(memberPlaceName).append("\n");
        stringBuilder.append("혼잡도: ").append(congestionLevelName);
        if (congestionMessage != null && !congestionMessage.isBlank()) {
            stringBuilder.append("\n").append("메시지: ").append(congestionMessage.strip());
        }

        return stringBuilder.toString();
    }

    private String fallback(
        String memberPlaceName,
        String congestionLevelName
    ) {
        return switch (congestionLevelName) {
            case "여유" -> "%s은(는) 한산한 편입니다.".formatted(memberPlaceName);
            case "약간 붐빔" -> "%s은(는) 다소 붐빕니다.".formatted(memberPlaceName);
            case "붐빔" -> "%s은(는) 매우 붐빕니다.".formatted(memberPlaceName);
            default -> "%s은(는) 보통 수준의 혼잡도입니다.".formatted(memberPlaceName);
        };
    }
}
