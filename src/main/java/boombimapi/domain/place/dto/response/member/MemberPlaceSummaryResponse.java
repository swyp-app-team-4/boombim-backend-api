package boombimapi.domain.place.dto.response.member;

import boombimapi.domain.place.entity.PlaceType;

public record MemberPlaceSummaryResponse(
    Long memberPlaceId,
    String name,
    PlaceType placeType,
    String address,
    Double latitude,
    Double longitude,
    String imageUrl,
    boolean isFavorite
) {

    public static MemberPlaceSummaryResponse of(
        Long memberPlaceId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl,
        boolean isFavorite
    ) {
        return new MemberPlaceSummaryResponse(
            memberPlaceId,
            name,
            PlaceType.MEMBER_PLACE,
            address,
            latitude,
            longitude,
            imageUrl,
            isFavorite
        );
    }
}