package boombimapi.domain.place.dto.request;

public record ResolveMemberPlaceRequest(
    String uuid,
    String memberPlaceName,
    Double latitude,
    Double longitude
) {

}
