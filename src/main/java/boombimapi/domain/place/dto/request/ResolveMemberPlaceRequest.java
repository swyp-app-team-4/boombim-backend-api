package boombimapi.domain.place.dto.request;

public record ResolveMemberPlaceRequest(
        String uuid,
        String name,
        Double latitude,
        Double longitude
) {

    public static ResolveMemberPlaceRequest of(
            String uuid,
            String name,
            Double latitude,
            Double longitude
    ) {
        return new ResolveMemberPlaceRequest(uuid, name, latitude, longitude);
    }
}
