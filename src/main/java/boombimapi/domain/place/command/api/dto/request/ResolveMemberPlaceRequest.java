package boombimapi.domain.place.command.api.dto.request;

public record ResolveMemberPlaceRequest(
    String uuid,
    String name,
    String address,
    Double latitude,
    Double longitude
) {

    public static ResolveMemberPlaceRequest of(
        String uuid,
        String name,
        String address,
        Double latitude,
        Double longitude
        ) {
        return new ResolveMemberPlaceRequest(
            uuid,
            name,
            address,
            latitude,
            longitude
        );
    }
}
