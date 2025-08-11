package boombimapi.domain.place.application.service.impl;

import boombimapi.domain.place.application.service.PlaceService;
import boombimapi.domain.place.domain.entity.OfficialPlace;
import boombimapi.domain.place.domain.repository.OfficialPlaceRepository;
import boombimapi.domain.place.presentation.dto.request.Viewport;
import boombimapi.domain.place.presentation.dto.response.MapMarker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialPlaceServiceImpl implements PlaceService {

    private final OfficialPlaceRepository officialPlaceRepository;

    @Override
    public List<MapMarker> getMarkersInViewport(
        Viewport viewport
    ) {

        double latitude1 = viewport.topLeft().latitude();
        double longitude1 = viewport.topLeft().longitude();
        double latitude2 = viewport.bottomRight().latitude();
        double longitude2 = viewport.bottomRight().longitude();

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
            .map(MapMarker::fromEntity)
            .toList();

    }
}
