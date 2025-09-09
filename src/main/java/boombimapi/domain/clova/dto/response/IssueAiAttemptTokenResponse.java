package boombimapi.domain.clova.dto.response;

import boombimapi.domain.clova.vo.AiAttemptId;

public record IssueAiAttemptTokenResponse(
    String aiAttemptId
) {

    public static IssueAiAttemptTokenResponse from(
        AiAttemptId aiAttemptId
    ) {
        return new IssueAiAttemptTokenResponse(
            aiAttemptId.value()
        );
    }

}
