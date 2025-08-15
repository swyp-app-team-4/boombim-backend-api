package boombimapi.domain.place.application;

import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.ViewportResponse;
import boombimapi.domain.place.entity.OfficialPlace;
import boombimapi.domain.place.repository.OfficialPlaceRepository;
import boombimapi.global.dto.Coordinate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialPlaceService {

    private final OfficialPlaceRepository officialPlaceRepository;
    private final OfficialCongestionRepository officialCongestionRepository;

    public List<ViewportResponse> getOfficialPlacesInViewport(
        ViewportRequest request
    ) {

        // 1. 뷰포트 범위 계산
        double lat1 = request.topLeft().latitude();
        double lng1 = request.topLeft().longitude();
        double lat2 = request.bottomRight().latitude();
        double lng2 = request.bottomRight().longitude();

        double minLatitude = Math.min(lat1, lat2);
        double maxLatitude = Math.max(lat1, lat2);
        double minLongitude = Math.min(lng1, lng2);
        double maxLongitude = Math.max(lng1, lng2);

        // 2. 뷰포트 내 공식 장소 리스트로 조회
        List<OfficialPlace> officialPlaces = findOfficialPlacesInViewport(
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        );

        double memberLatitude = request.memberCoordinate().latitude();
        double memberLongitude = request.memberCoordinate().longitude();

        // 3. 각 공식 장소별 최신 혼잡도 조회
        List<ViewportResponse> result = new ArrayList<>(officialPlaces.size());

        for (OfficialPlace officialPlace : officialPlaces) {
            Optional<OfficialCongestion> optionalLatestOfficialCongestion = officialCongestionRepository
                .findTopByOfficialPlaceIdOrderByObservedAtDesc(officialPlace.getId());

            if (optionalLatestOfficialCongestion.isEmpty()) {
                continue;
            }

            OfficialCongestion latestOfficialCongestion = optionalLatestOfficialCongestion.get();
            CongestionLevel congestionLevel = latestOfficialCongestion.getCongestionLevel();

            Double centroidLatitude = officialPlace.getCentroidLatitude();
            Double centroidLongitude = officialPlace.getCentroidLongitude();

            double distanceMeters = haversine(
                memberLatitude,
                memberLongitude,
                centroidLatitude,
                centroidLongitude
            );

            result.add(new ViewportResponse(
                officialPlace.getId(),
                officialPlace.getName(),
                new Coordinate(centroidLatitude, centroidLongitude),
                distanceMeters,
                congestionLevel.getName(),
                congestionLevel.getMessage()));
        }

        return result;
    }

    private List<OfficialPlace> findOfficialPlacesInViewport(
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    ) {
        return officialPlaceRepository
            .findByCentroidLatitudeBetweenAndCentroidLongitudeBetween(
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude
            );
    }

    private double haversine(double aLat, double aLng, double bLat, double bLng) {
        double dLat = Math.toRadians(bLat - aLat);
        double dLng = Math.toRadians(bLng - aLng);
        double s = Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(aLat)) * Math.cos(Math.toRadians(bLat))
            * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * 6_371_000 * Math.atan2(Math.sqrt(s), Math.sqrt(1 - s));
    }

}
