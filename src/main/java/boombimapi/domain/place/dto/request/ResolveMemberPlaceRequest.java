package boombimapi.domain.place.dto.request;

public record ResolveMemberPlaceRequest(
        String uuid,
        String name,
        Double latitude,
        Double longitude,

        String imageUrl
) {

    public static ResolveMemberPlaceRequest of(
            String uuid,
            String name,
            Double latitude,
            Double longitude,
            String imageUrl
            ) {
        return new ResolveMemberPlaceRequest(uuid, name, latitude, longitude, imageUrl);
    }
}
