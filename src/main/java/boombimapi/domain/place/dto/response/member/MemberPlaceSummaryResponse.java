package boombimapi.domain.place.dto.response.member;

public record MemberPlaceSummaryResponse(
    Long memberPlaceId,
    String name,
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
            address,
            latitude,
            longitude,
            imageUrl,
            isFavorite
        );
    }
}