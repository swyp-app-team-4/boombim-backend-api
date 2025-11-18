package boombimapi.global.geo;

import boombimapi.domain.place.query.api.dto.request.ViewportRequest;

public record ViewportBounds(
    double minLatitude,
    double maxLatitude,
    double minLongitude,
    double maxLongitude
) {

    public static ViewportBounds from(
        ViewportRequest request
    ) {
        double latitude1 = request.topLeft().latitude();
        double longitude1 = request.topLeft().longitude();
        double latitude2 = request.bottomRight().latitude();
        double longitude2 = request.bottomRight().longitude();

        double minLatitude = Math.min(latitude1, latitude2);
        double maxLatitude = Math.max(latitude1, latitude2);
        double minLongitude = Math.min(longitude1, longitude2);
        double maxLongitude = Math.max(longitude1, longitude2);

        return new ViewportBounds(
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        );
    }

}
