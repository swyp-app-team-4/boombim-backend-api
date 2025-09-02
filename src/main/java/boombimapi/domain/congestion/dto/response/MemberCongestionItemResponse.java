package boombimapi.domain.congestion.dto.response;

import boombimapi.domain.congestion.entity.MemberCongestion;
import java.time.LocalDateTime;

public record MemberCongestionItemResponse(
    Long memberCongestionId,
    String congestionLevelName,
    String congestionLevelMessage,
    LocalDateTime createdAt
) {

    public static MemberCongestionItemResponse of(
        MemberCongestion memberCongestion
    ) {
        return new MemberCongestionItemResponse(
            memberCongestion.getId(),
            memberCongestion.getCongestionLevel().getName(),
            memberCongestion.getCongestionMessage(),
            memberCongestion.getCreatedAt()
        );
    }

}
