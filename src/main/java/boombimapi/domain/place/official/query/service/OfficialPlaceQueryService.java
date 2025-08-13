package boombimapi.domain.place.official.query.service;

import static boombimapi.global.constant.PlaceConstant.*;

import boombimapi.domain.place.official.query.repository.OfficialPlaceQueryRepository;
import boombimapi.domain.place.official.query.dto.request.ViewportRequest;
import boombimapi.domain.place.official.query.dto.response.MapMarkerResponse;
import boombimapi.domain.place.shared.dto.PlaceInfo;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialPlaceQueryService {

    private final OfficialPlaceQueryRepository officialPlaceQueryRepository;

    public List<MapMarkerResponse> getMarkersInViewport(
        ViewportRequest viewportRequest
    ) {

        // TODO: 사용자 좌표 없을 시의 예외 처리

        double latitude1 = viewportRequest.topLeft().latitude();
        double longitude1 = viewportRequest.topLeft().longitude();
        double latitude2 = viewportRequest.bottomRight().latitude();
        double longitude2 = viewportRequest.bottomRight().longitude();

        double minLatitude = Math.min(latitude1, latitude2);
        double maxLatitude = Math.max(latitude1, latitude2);
        double minLongitude = Math.min(longitude1, longitude2);
        double maxLongitude = Math.max(longitude1, longitude2);

        List<PlaceInfo> viewportOfficialPlaces = officialPlaceQueryRepository.findViewportOfficialPlaces(
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        );

        double memberLatitude = viewportRequest.memberCoordinate().latitude();
        double memberLongitude = viewportRequest.memberCoordinate().longitude();

        return viewportOfficialPlaces.stream()
            .map(place -> new MapMarkerResponse(
                place.id(),
                place.name(),
                place.coordinate(),
                calculateHaversineMeter(
                    memberLatitude,
                    memberLongitude,
                    place.coordinate().latitude(),
                    place.coordinate().longitude()
                )
            ))
            .sorted(Comparator.comparingDouble(MapMarkerResponse::distance))
            .toList();
    }

    private double calculateHaversineMeter(
        double memberLatitude,
        double memberLongitude,
        double targetLatitude,
        double targetLongitude
    ) {
        double deltaLatitudeRadians = Math.toRadians(targetLatitude - memberLatitude);
        double deltaLongitudeRadians = Math.toRadians(targetLongitude - memberLongitude);

        double v = Math.pow(Math.sin(deltaLatitudeRadians / 2), 2)
            + Math.cos(Math.toRadians(memberLatitude))
            * Math.cos(Math.toRadians(targetLatitude))
            * Math.pow(Math.sin(deltaLongitudeRadians / 2), 2);

        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(v), Math.sqrt(1 - v));
    }
}
