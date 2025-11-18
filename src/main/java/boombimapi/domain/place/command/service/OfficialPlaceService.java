package boombimapi.domain.place.command.service;

import static boombimapi.domain.place.shared.type.PlaceType.OFFICIAL_PLACE;
import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.OfficialCongestionDemographicsRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionForecastRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.congestion.repository.OfficialPlaceCongestionRankProjection;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.place.query.api.dto.response.official.CongestedOfficialPlaceResponse;
import boombimapi.domain.place.query.api.dto.response.official.NearbyNonCongestedOfficialPlaceResponse;
import boombimapi.domain.place.query.api.dto.response.official.OfficialPlaceDemographics;
import boombimapi.domain.place.query.api.dto.response.official.OfficialPlaceForecast;
import boombimapi.domain.place.query.api.dto.response.official.OfficialPlaceOverviewResponse;
import boombimapi.domain.place.command.entity.OfficialPlace;
import boombimapi.domain.place.command.repository.OfficialPlaceRepository;
import boombimapi.domain.place.query.dao.projection.NearbyNonCongestedOfficialPlaceProjection;
import boombimapi.global.infra.exception.error.BoombimException;
import java.util.ArrayList;
import java.util.List;
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

    // TODO: OfficialPlaceQueryService에서 CQRS로 전환 예정
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
            latestOfficialCongestion.getPopulationMin(),
            latestOfficialCongestion.getPopulationMax(),
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
