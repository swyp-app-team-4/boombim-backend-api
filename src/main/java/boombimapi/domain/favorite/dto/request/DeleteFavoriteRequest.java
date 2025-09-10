package boombimapi.domain.favorite.dto.request;

public record DeleteFavoriteRequest(
    String memberId,
    Long memberPlaceId
) {

}
