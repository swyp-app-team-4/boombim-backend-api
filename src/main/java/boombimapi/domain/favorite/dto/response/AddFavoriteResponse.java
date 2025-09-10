package boombimapi.domain.favorite.dto.response;

public record AddFavoriteResponse(
    Long favoriteId
) {

    public static AddFavoriteResponse from(
        Long favoriteId
    ) {
        return new AddFavoriteResponse(favoriteId);
    }

}
