package boombimapi.domain.clova.application;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.clova.dto.request.ClovaChatRequest;
import boombimapi.domain.clova.dto.request.GenerateCongestionMessageRequest;
import boombimapi.domain.clova.dto.request.IssueAiAttemptTokenRequest;
import boombimapi.domain.clova.dto.response.ClovaResponse;
import boombimapi.domain.clova.dto.response.GenerateCongestionMessageResponse;
import boombimapi.domain.clova.dto.response.IssueAiAttemptTokenResponse;
import boombimapi.domain.clova.infrastructure.ClovaCongestionMessageClient;
import boombimapi.domain.clova.infrastructure.parser.ClovaParser;
import boombimapi.domain.clova.infrastructure.repository.AiAttemptTokenRepository;
import boombimapi.domain.clova.vo.AiAttemptToken;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.RateLimitedException;
import boombimapi.global.infra.ratelimit.AiAttemptTokenBucketLimiter;
import boombimapi.global.properties.AiAttemptTokenBucketProperties;
import boombimapi.global.properties.ClovaGenerationProperties;
import boombimapi.global.properties.ClovaPromptProperties;
import boombimapi.global.vo.AiAttemptRateLimitDecision;
import java.time.Duration;
import java.util.Map;
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
    private final AiAttemptTokenBucketProperties aiAttemptTokenBucketProperties;

    private final AiAttemptTokenBucketLimiter aiAttemptTokenBucketLimiter;
    private final ClovaCongestionMessageClient clovaCongestionMessageClient;

    private final AiAttemptTokenGenerator aiAttemptTokenGenerator;
    private final AiAttemptTokenRepository aiAttemptTokenRepository;

    public IssueAiAttemptTokenResponse issueAiAttemptToken(
        String memberId,
        IssueAiAttemptTokenRequest request
    ) {
        AiAttemptToken aiAttemptToken = aiAttemptTokenGenerator.generateAiAttemptToken();
        aiAttemptTokenRepository.saveAiAttemptToken(aiAttemptToken, memberId, request.memberPlaceId());
        aiAttemptTokenRepository.setActiveAiAttemptPointer(memberId, aiAttemptToken);

        return IssueAiAttemptTokenResponse.from(aiAttemptToken);
    }

    public GenerateCongestionMessageResponse generateCongestionMessage(
        String memberId,
        GenerateCongestionMessageRequest request
    ) {

        AiAttemptRateLimitDecision aiAttemptRateLimitDecision = aiAttemptTokenBucketLimiter.checkAndConsume(
            memberId,
            aiAttemptTokenBucketProperties.defaultCost()
        );

        if (!aiAttemptRateLimitDecision.allowed()) {
            long retryAfterSeconds = aiAttemptRateLimitDecision.retryAfterSeconds();
            throw new RateLimitedException(AI_ATTEMPT_RATE_LIMITED, retryAfterSeconds);
        }

        validateAiAttemptToken(memberId, request);

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

    private void validateAiAttemptToken(
        String memberId,
        GenerateCongestionMessageRequest request
    ) {
        AiAttemptToken providedAiAttemptToken = new AiAttemptToken(request.aiAttemptToken());

        Map<Object, Object> aiAttemptMeta = aiAttemptTokenRepository.getAiAttemptMeta(providedAiAttemptToken)
            .orElseThrow(() -> new BoombimException(AI_ATTEMPT_TOKEN_NOT_FOUND));

        log.info("aiAttemptMeta: {}", aiAttemptMeta);

        if (!aiAttemptMeta.get("memberId").equals(memberId)) {
            throw new BoombimException(AI_ATTEMPT_TOKEN_MEMBER_MISMATCH);
        }

        log.info("aiAttemptMeta memberPlaceId: {}", aiAttemptMeta.get("memberPlaceId"));
        log.info("request.memberPlaceId: {}", request.memberPlaceId());

        if (!aiAttemptMeta.get("memberPlaceId").equals(request.memberPlaceId().toString())) {
            throw new BoombimException(AI_ATTEMPT_TOKEN_PLACE_MISMATCH);
        }

        AiAttemptToken activeAiAttemptToken = aiAttemptTokenRepository.getActiveAiAttemptPointer(memberId)
            .orElseThrow(() -> new BoombimException(AI_ATTEMPT_TOKEN_NO_ACTIVE_POINTER));

        if (!activeAiAttemptToken.value().equals(providedAiAttemptToken.value())) {
            throw new BoombimException(AI_ATTEMPT_TOKEN_SUPERSEDED);
        }

        boolean first = aiAttemptTokenRepository.acquireOnce(providedAiAttemptToken);

        if (!first) {
            throw new BoombimException(AI_ATTEMPT_TOKEN_ALREADY_USED);
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
