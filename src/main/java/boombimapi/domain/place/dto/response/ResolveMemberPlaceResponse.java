package boombimapi.domain.place.dto.response;

import boombimapi.domain.place.entity.MemberPlace;

public record ResolveMemberPlaceResponse(
    Long id
) {

    public static ResolveMemberPlaceResponse from(
        MemberPlace memberPlace
    ) {
        return new ResolveMemberPlaceResponse(
            memberPlace.getId()
        );
    }

}
