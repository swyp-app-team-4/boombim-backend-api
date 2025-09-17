package boombimapi.domain.place.query.dao.param;

import boombimapi.domain.place.query.api.dto.request.ViewportRequest;

public record OfficialPlaceViewportParam(
    String memberId,
    double minLatitude,
    double maxLatitude,
    double minLongitude,
    double maxLongitude
) {

    public static OfficialPlaceViewportParam from(
        String memberId,
        ViewportRequest request
    ) {
        double minLatitude = Math.min(request.topLeft().latitude(), request.bottomRight().latitude());
        double maxLatitude = Math.max(request.topLeft().latitude(), request.bottomRight().latitude());

        double minLongitude = Math.min(request.topLeft().longitude(), request.bottomRight().longitude());
        double maxLongitude = Math.max(request.topLeft().longitude(), request.bottomRight().longitude());

        return new OfficialPlaceViewportParam(
            memberId,
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        );
    }

}
