package boombimapi.domain.favorite.dto.response;

public record GetFavoriteResponse(
    Long favoriteId,
    Long memberPlaceId
) {

    public static GetFavoriteResponse from(
        Long favoriteId,
        Long memberPlaceId
    ) {
        return new GetFavoriteResponse(
            favoriteId,
            memberPlaceId
        );
    }

}
