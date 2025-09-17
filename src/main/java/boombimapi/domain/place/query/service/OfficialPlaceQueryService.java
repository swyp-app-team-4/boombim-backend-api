package boombimapi.domain.place.query.service;

import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.ViewportResponse;
import boombimapi.domain.place.query.dao.OfficialPlaceQueryDao;
import boombimapi.domain.place.query.dao.param.OfficialPlaceViewportParam;
import boombimapi.domain.place.query.dao.row.OfficialPlaceViewportRow;
import boombimapi.global.vo.Coordinate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialPlaceQueryService {

    private final OfficialPlaceQueryDao officialPlaceQueryDao;

    public List<ViewportResponse> getOfficialPlacesInViewport(
        String memberId,
        ViewportRequest request
    ) {
        OfficialPlaceViewportParam param = OfficialPlaceViewportParam.from(memberId, request);

        List<OfficialPlaceViewportRow> viewportRows = officialPlaceQueryDao.findInViewport(param);

        log.info("[OfficialPlaceQueryService] getOfficialPlacesInViewport() viewportRows.size(): {}", viewportRows.size());

        if (viewportRows.isEmpty()) {
            return List.of();
        }

        Coordinate memberCoordinate = request.memberCoordinate();
        double memberLatitude = memberCoordinate.latitude();
        double memberLongitude = memberCoordinate.longitude();

        List<ViewportResponse> result = new ArrayList<>(viewportRows.size());

        for (OfficialPlaceViewportRow row : viewportRows) {
            Coordinate rowCoordinate = Coordinate.of(row.centroidLatitude(), row.centroidLongitude());

            double distance = haversine(
                memberLatitude,
                memberLongitude,
                row.centroidLatitude(),
                row.centroidLongitude()
            );

            result.add(
                ViewportResponse.of(
                    row.id(),
                    row.name(),
                    row.legalDong(),
                    row.imageUrl(),
                    rowCoordinate,
                    distance,
                    row.congestionLevelName(),
                    row.congestionMessage(),
                    Boolean.TRUE.equals(row.isFavorite())
                )
            );
        }

        result.sort(Comparator.comparingDouble(ViewportResponse::distance));

        return result;
    }

    private double haversine(
        double aLatitude,
        double aLongitude,
        double bLatitude,
        double bLongitude
    ) {
        double dLat = Math.toRadians(bLatitude - aLatitude);
        double dLng = Math.toRadians(bLongitude - aLongitude);
        double s = Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(aLatitude)) * Math.cos(Math.toRadians(bLatitude))
            * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * 6_371_000 * Math.atan2(Math.sqrt(s), Math.sqrt(1 - s));
    }

}
