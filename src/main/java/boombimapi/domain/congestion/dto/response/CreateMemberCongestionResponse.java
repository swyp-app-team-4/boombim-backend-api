package boombimapi.domain.congestion.dto.response;

import boombimapi.domain.congestion.entity.MemberCongestion;

public record CreateMemberCongestionResponse(
    Long memberCongestionId,
    String memberPlaceName,
    boolean pointReceived
) {

    public static CreateMemberCongestionResponse of(
        MemberCongestion memberCongestion,
        boolean pointReceived
    ) {
        return new CreateMemberCongestionResponse(
            memberCongestion.getId(),
            memberCongestion.getMemberPlace().getName(),
            pointReceived
        );
    }

}
