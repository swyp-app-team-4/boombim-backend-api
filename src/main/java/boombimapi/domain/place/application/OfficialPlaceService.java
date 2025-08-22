package boombimapi.domain.place.application;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.OfficialCongestionDemographicsRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionForecastRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.OfficialPlaceDemographics;
import boombimapi.domain.place.dto.response.OfficialPlaceForecast;
import boombimapi.domain.place.dto.response.OfficialPlaceOverviewResponse;
import boombimapi.domain.place.dto.response.ViewportResponse;
import boombimapi.domain.place.entity.OfficialPlace;
import boombimapi.domain.place.repository.OfficialPlaceRepository;
import boombimapi.global.dto.Coordinate;
import boombimapi.global.infra.exception.error.BoombimException;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final OfficialCongestionForecastRepository forecastRepository;
    private final OfficialCongestionRepository officialCongestionRepository;
    private final OfficialCongestionDemographicsRepository demographicsRepository;

    public List<ViewportResponse> getOfficialPlacesInViewport(
        ViewportRequest request
    ) {

        // TODO: 직선 거리 계산 부분 리팩터링 필요
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
        List<OfficialPlace> officialPlaces = officialPlaceRepository
            .findByCentroidLatitudeBetweenAndCentroidLongitudeBetween(
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

        // TODO: 정렬을 DB 단에서 해줄지, 아니면 지금처럼 소스 코드 단에서 해줄지 고민
        result.sort(Comparator.comparingDouble(ViewportResponse::distance));

        return result;
    }

    public OfficialPlaceOverviewResponse getOverview(
        Long officialPlaceId
    ) {

        OfficialPlace officialPlace = officialPlaceRepository
            .findById(officialPlaceId)
            .orElseThrow(() -> new BoombimException(OFFICIAL_PLACE_NOT_FOUND));

        OfficialCongestion latestOfficialCongestion = officialCongestionRepository
            .findTopByOfficialPlaceIdOrderByObservedAtDesc(officialPlace.getId())
            .orElseThrow(() -> new BoombimException(OFFICIAL_CONGESTION_NOT_FOUND));

        List<OfficialPlaceDemographics> demographics = demographicsRepository
            .findByOfficialCongestion(latestOfficialCongestion)
            .stream()
            .map(OfficialPlaceDemographics::from)
            .toList();

        List<OfficialPlaceForecast> forecasts = forecastRepository
            .findByOfficialPlaceAndObservedAtOrderByForecastTimeAsc(
                officialPlace,
                latestOfficialCongestion.getObservedAt()
            )
            .stream()
            .map(OfficialPlaceForecast::from)
            .toList();

        return new OfficialPlaceOverviewResponse(
            officialPlace.getId(),
            officialPlace.getName(),
            officialPlace.getPoiCode(),
            officialPlace.getCentroidLatitude(),
            officialPlace.getCentroidLongitude(),
            officialPlace.getPolygonCoordinates(),
            demographics,
            forecasts
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
