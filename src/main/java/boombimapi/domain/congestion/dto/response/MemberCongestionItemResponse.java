package boombimapi.domain.congestion.dto.response;

import java.time.LocalDateTime;

public record MemberCongestionItemResponse(
    Long memberCongestionId,
    String memberProfile,
    String memberName,
    String congestionLevelName,
    String congestionLevelMessage,
    LocalDateTime createdAt
) {

    public static MemberCongestionItemResponse of(
        Long memberCongestionId,
        String memberProfile,
        String memberName,
        String congestionLevelName,
        String congestionLevelMessage,
        LocalDateTime createdAt
    ) {
        return new MemberCongestionItemResponse(
            memberCongestionId,
            memberProfile,
            memberName,
            congestionLevelName,
            congestionLevelMessage,
            createdAt
        );
    }
}
