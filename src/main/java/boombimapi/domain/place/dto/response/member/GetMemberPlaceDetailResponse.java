package boombimapi.domain.place.dto.response.member;

import boombimapi.domain.congestion.dto.response.MemberCongestionItemResponse;
import java.util.List;

public record GetMemberPlaceDetailResponse(
    MemberPlaceSummaryResponse memberPlaceSummary,
    List<MemberCongestionItemResponse> memberCongestionItems,
    boolean hasNext,
    Long nextCursor,
    int size
) {

    public static GetMemberPlaceDetailResponse of(
        MemberPlaceSummaryResponse memberPlaceSummary,
        List<MemberCongestionItemResponse> memberCongestionItems,
        boolean hasNext,
        Long nextCursor,
        int size
    ) {
        return new GetMemberPlaceDetailResponse(
            memberPlaceSummary,
            memberCongestionItems,
            hasNext,
            nextCursor,
            size
        );
    }

}
