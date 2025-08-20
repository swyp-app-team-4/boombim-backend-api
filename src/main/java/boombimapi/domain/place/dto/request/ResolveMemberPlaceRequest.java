package boombimapi.domain.place.dto.request;

public record ResolveMemberPlaceRequest(
    String uuid,
    String name,
    Double latitude,
    Double longitude
) {

}
