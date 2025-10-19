package boombimapi.domain.place.command.service;

import static boombimapi.domain.place.shared.type.PlaceType.OFFICIAL_PLACE;
import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.OfficialCongestionDemographicsRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionForecastRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.congestion.repository.OfficialPlaceCongestionRankProjection;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.official.CongestedOfficialPlaceResponse;
import boombimapi.domain.place.query.api.dto.response.official.NearbyNonCongestedOfficialPlaceResponse;
import boombimapi.domain.place.query.api.dto.response.official.OfficialPlaceDemographics;
import boombimapi.domain.place.query.api.dto.response.official.OfficialPlaceForecast;
import boombimapi.domain.place.query.api.dto.response.official.OfficialPlaceOverviewResponse;
import boombimapi.domain.place.query.api.dto.response.ViewportResponse;
import boombimapi.domain.place.command.entity.OfficialPlace;
import boombimapi.domain.place.command.repository.OfficialPlaceRepository;
import boombimapi.domain.place.query.dao.projection.NearbyNonCongestedOfficialPlaceProjection;
import boombimapi.global.geo.GeoDistance;
import boombimapi.global.vo.Coordinate;
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
    private final FavoriteRepository favoriteRepository;

    // TODO: deprecated 된 메서드 -> 프론트에 전달 후 삭제 예정
    public List<ViewportResponse> getOfficialPlacesInViewport(
        String memberId,
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

            double distanceMeters = GeoDistance.haversineMeters(
                memberLatitude,
                memberLongitude,
                centroidLatitude,
                centroidLongitude
            );

            Coordinate coordinate = Coordinate.of(centroidLatitude, centroidLongitude);

            boolean isFavorite = isFavorite(memberId, officialPlace.getId());

            result.add(
                ViewportResponse.of(
                    officialPlace.getId(),
                    officialPlace.getName(),
                    officialPlace.getLegalDong(),
                    officialPlace.getImageUrl(),
                    coordinate,
                    distanceMeters,
                    congestionLevel.getName(),
                    congestionLevel.getMessage(),
                    isFavorite
                )
            );
        }

        // TODO: 정렬을 DB 단에서 해줄지, 아니면 지금처럼 소스 코드 단에서 해줄지 고민
        result.sort(Comparator.comparingDouble(ViewportResponse::distance));

        return result;
    }

    public OfficialPlaceOverviewResponse getOverview(
        String memberId,
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

        boolean isFavorite = isFavorite(memberId, officialPlaceId);

        return OfficialPlaceOverviewResponse.of(
            officialPlace.getId(),
            officialPlace.getName(),
            officialPlace.getLegalDong(),
            officialPlace.getPoiCode(),
            officialPlace.getImageUrl(),
            latestOfficialCongestion.getCongestionLevel().getName(),
            latestOfficialCongestion.getCongestionLevel().getMessage(),
            latestOfficialCongestion.getObservedAt(),
            officialPlace.getCentroidLatitude(),
            officialPlace.getCentroidLongitude(),
            officialPlace.getPolygonCoordinates(),
            demographics,
            forecasts,
            isFavorite
        );
    }

    public List<NearbyNonCongestedOfficialPlaceResponse> getNearbyNonCongestedOfficialPlace(
        double latitude,
        double longitude
    ) {
        int limit = 10;

        List<NearbyNonCongestedOfficialPlaceProjection> rows = officialPlaceRepository
            .findNearbyNonCongestedOfficialPlace(latitude, longitude, limit);

        ArrayList<NearbyNonCongestedOfficialPlaceResponse> result = new ArrayList<>(rows.size());

        for (NearbyNonCongestedOfficialPlaceProjection row : rows) {
            result.add(NearbyNonCongestedOfficialPlaceResponse.from(row));
        }

        return result;
    }

    public List<CongestedOfficialPlaceResponse> getCongestedOfficialPlace() {
        int limit = 5;

        List<OfficialPlaceCongestionRankProjection> rows = officialCongestionRepository
            .findTopCongestedOfficialPlace(limit);

        List<CongestedOfficialPlaceResponse> result = new ArrayList<>(rows.size());

        for (OfficialPlaceCongestionRankProjection row : rows) {
            result.add(CongestedOfficialPlaceResponse.from(row));
        }

        return result;
    }

    private boolean isFavorite(
        String memberId,
        Long placeId
    ) {
        if (memberId == null) {
            return false;
        }

        return favoriteRepository.existsByMemberIdAndPlaceIdAndPlaceType(
            memberId,
            placeId,
            OFFICIAL_PLACE
        );
    }

}
