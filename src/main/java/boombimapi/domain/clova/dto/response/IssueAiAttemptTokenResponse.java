package boombimapi.domain.clova.dto.response;

import boombimapi.domain.clova.vo.AiAttemptToken;

public record IssueAiAttemptTokenResponse(
    String aiAttemptToken
) {

    public static IssueAiAttemptTokenResponse from(
        AiAttemptToken aiAttemptToken
    ) {
        return new IssueAiAttemptTokenResponse(
            aiAttemptToken.value()
        );
    }

}
