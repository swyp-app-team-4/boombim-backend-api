package boombimapi.domain.congestion.dto.response;

import boombimapi.domain.congestion.entity.MemberCongestion;

public record CreateMemberCongestionResponse(
    Long memberCongestionId,
    String memberPlaceName
) {

    public static CreateMemberCongestionResponse from(
        MemberCongestion memberCongestion
    ) {
        return new CreateMemberCongestionResponse(
            memberCongestion.getId(),
            memberCongestion.getMemberPlace().getName()
        );
    }

}
