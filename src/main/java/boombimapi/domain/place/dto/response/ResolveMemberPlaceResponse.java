package boombimapi.domain.place.dto.response;

public record ResolveMemberPlaceResponse(
    Long memberPlaceId,
    String uuid,
    String memberPlaceName
) {

}
