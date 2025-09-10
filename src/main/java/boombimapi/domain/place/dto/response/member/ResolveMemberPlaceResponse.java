package boombimapi.domain.place.dto.response.member;

import boombimapi.domain.place.entity.MemberPlace;

public record ResolveMemberPlaceResponse(
    Long memberPlaceId
) {

    public static ResolveMemberPlaceResponse from(
        MemberPlace memberPlace
    ) {
        return new ResolveMemberPlaceResponse(
            memberPlace.getId()
        );
    }

}
