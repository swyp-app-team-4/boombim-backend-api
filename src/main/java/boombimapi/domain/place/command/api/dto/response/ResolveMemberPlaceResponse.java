package boombimapi.domain.place.command.api.dto.response;

import boombimapi.domain.place.command.entity.MemberPlace;

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
