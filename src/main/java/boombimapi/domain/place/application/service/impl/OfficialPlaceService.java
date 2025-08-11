package boombimapi.domain.place.application.service.impl;

import boombimapi.domain.place.domain.entity.OfficialPlace;
import boombimapi.domain.place.domain.repository.OfficialPlaceRepository;
import boombimapi.domain.place.presentation.dto.request.ViewportRequest;
import boombimapi.domain.place.presentation.dto.response.MapMarkerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialPlaceService {

    private final OfficialPlaceRepository officialPlaceRepository;

    public List<MapMarkerResponse> getMarkersInViewport(
        ViewportRequest viewportRequest
    ) {

        double latitude1 = viewportRequest.topLeft().latitude();
        double longitude1 = viewportRequest.topLeft().longitude();
        double latitude2 = viewportRequest.bottomRight().latitude();
        double longitude2 = viewportRequest.bottomRight().longitude();

        double minLatitude = Math.min(latitude1, latitude2);
        double maxLatitude = Math.max(latitude1, latitude2);
        double minLongitude = Math.min(longitude1, longitude2);
        double maxLongitude = Math.max(longitude1, longitude2);

        List<OfficialPlace> officialPlaces = officialPlaceRepository.findInViewport(
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        );

        return officialPlaces.stream()
            .map(MapMarkerResponse::fromEntity)
            .toList();

    }
}
