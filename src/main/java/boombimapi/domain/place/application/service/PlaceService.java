package boombimapi.domain.place.application.service;

import boombimapi.domain.place.presentation.dto.request.ViewportRequest;
import boombimapi.domain.place.presentation.dto.response.MapMarker;
import java.util.List;

public interface PlaceService {

    List<MapMarker> getMarkersInViewport(
        ViewportRequest viewportRequest
    );

}
