package boombimapi.domain.place.query.dao.param;

import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.global.geo.ViewportBounds;

public record ViewportParam(
    String memberId,
    double minLatitude,
    double maxLatitude,
    double minLongitude,
    double maxLongitude
) {

    public static ViewportParam from(
        String memberId,
        ViewportRequest request
    ) {
        ViewportBounds bounds = ViewportBounds.from(request);

        return new ViewportParam(
            memberId,
            bounds.minLatitude(),
            bounds.maxLatitude(),
            bounds.minLongitude(),
            bounds.maxLongitude()
        );
    }

}
