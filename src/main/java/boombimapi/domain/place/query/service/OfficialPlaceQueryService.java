package boombimapi.domain.place.query.service;

import boombimapi.domain.place.cluster.Clusterer;
import boombimapi.domain.place.cluster.WebMercator;
import boombimapi.domain.place.cluster.vo.ClusterInput;
import boombimapi.domain.place.cluster.vo.ClusterResult;
import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.ViewportResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportClusterMarkerResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportMarkerResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportPlaceMarkerResponse;
import boombimapi.domain.place.query.dao.OfficialPlaceQueryDao;
import boombimapi.domain.place.query.dao.param.ViewportParam;
import boombimapi.domain.place.query.dao.row.OfficialPlaceViewportRow;
import boombimapi.domain.place.shared.type.PlaceType;
import boombimapi.global.geo.GeoDistance;
import boombimapi.global.properties.ClusterProperties;
import boombimapi.global.vo.Coordinate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        ViewportParam param = ViewportParam.from(memberId, request);

        List<OfficialPlaceViewportRow> viewportRows = officialPlaceQueryDao.findInViewport(param);

        if (viewportRows.isEmpty()) {
            return List.of();
        }

        Coordinate memberCoordinate = request.memberCoordinate();
        double memberLatitude = memberCoordinate.latitude();
        double memberLongitude = memberCoordinate.longitude();

        List<ViewportResponse> result = new ArrayList<>(viewportRows.size());

        for (OfficialPlaceViewportRow row : viewportRows) {
            Coordinate rowCoordinate = Coordinate.of(row.centroidLatitude(), row.centroidLongitude());

            double distance = GeoDistance.haversineMeters(
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

    public List<ViewportMarkerResponse> getOfficialPlacesClusteredInViewport(
        String memberId,
        ViewportRequest request,
        Clusterer clusterer,
        ClusterProperties properties
    ) {

        ViewportParam viewportParam = ViewportParam.from(memberId, request);

        List<OfficialPlaceViewportRow> rows = officialPlaceQueryDao.findInViewport(viewportParam);
        if (rows.isEmpty())
            return List.of();

        final int zoomLevel = request.zoomLevel();
        final boolean isMaxZoom = zoomLevel == properties.maxZoomAtRefZ();

        final double memberLatitude = request.memberCoordinate().latitude();
        final double memberLongitude = request.memberCoordinate().longitude();

        // 1. 최대 줌 레벨에서는 클러스터링 X
        if (isMaxZoom) {
            List<ViewportMarkerResponse> markers = new ArrayList<>(rows.size());
            for (OfficialPlaceViewportRow row : rows) {

                double distance = GeoDistance.haversineMeters(
                    memberLatitude,
                    memberLongitude,
                    row.centroidLatitude(),
                    row.centroidLongitude()
                );

                markers.add(
                    ViewportPlaceMarkerResponse.of(
                        row.id(),
                        row.name(),
                        PlaceType.OFFICIAL_PLACE,
                        Coordinate.of(row.centroidLatitude(), row.centroidLongitude()),
                        distance,
                        row.congestionLevelName(),
                        row.congestionMessage(),
                        row.observedAt(),
                        Boolean.TRUE.equals(row.isFavorite()),
                        null
                    )
                );
            }

            // TODO: 가까운 거리 순으로 마커들의 정렬이 필요할지?
            // markers.sort(Comparator.comparingDouble(m -> ((ViewportPlaceMarkerResponse) m).distance()));
            return markers;
        }

        // 2. 클러스터링을 위한 변환
        List<ClusterInput> inputs = new ArrayList<>(rows.size());
        for (OfficialPlaceViewportRow row : rows) {
            inputs.add(new ClusterInput(
                row.id(),
                row.centroidLatitude(),
                row.centroidLongitude()
            ));
        }

        // 3. 클러스터 실행
        List<ClusterResult> clusterResults = clusterer.cluster(inputs, zoomLevel);

        Map<Long, OfficialPlaceViewportRow> rowHashMap = new HashMap<>(rows.size());
        for (OfficialPlaceViewportRow row : rows) {
            rowHashMap.put(row.id(), row);
        }

        final int minClusterSize = properties.minClusterSize();
        final int refZ = properties.refZ();
        final double tileSize = properties.tileSize();

        List<ViewportMarkerResponse> markers = new ArrayList<>(clusterResults.size());

        for (ClusterResult clusterResult : clusterResults) {
            // MarkerType == CLUSTER
            if (clusterResult.count() >= minClusterSize) {
                double lng = WebMercator.worldPixelXToLongitude(clusterResult.centroidWorldPixelX(), refZ, tileSize);
                double lat = WebMercator.worldPixelYToLatitude(clusterResult.centroidWorldPixelY(), refZ, tileSize);

                // 혼잡도 수준 카운트 (보류)
                Map<String, Integer> levelCounts = new HashMap<>();

                for (Long placeId : clusterResult.placeIds()) {
                    OfficialPlaceViewportRow row = rowHashMap.get(placeId);
                    if (row == null)
                        continue;
                    String level = row.congestionLevelName();
                    if (level != null)
                        levelCounts.merge(level, 1, Integer::sum);
                }

                markers.add(
                    ViewportClusterMarkerResponse.of(
                        Coordinate.of(lat, lng),
                        clusterResult.count(),
                        levelCounts
                    )
                );
                continue;
            }

            // MarkerType == PLACE
            for (Long placeId : clusterResult.placeIds()) {
                OfficialPlaceViewportRow row = rowHashMap.get(placeId);
                if (row == null)
                    continue;

                double distance = GeoDistance.haversineMeters(
                    memberLatitude,
                    memberLongitude,
                    row.centroidLatitude(),
                    row.centroidLongitude()
                );

                markers.add(
                    ViewportPlaceMarkerResponse.of(
                        row.id(),
                        row.name(),
                        PlaceType.OFFICIAL_PLACE,
                        Coordinate.of(row.centroidLatitude(), row.centroidLongitude()),
                        distance,
                        row.congestionLevelName(),
                        row.congestionMessage(),
                        row.observedAt(),
                        Boolean.TRUE.equals(row.isFavorite()),
                        null
                    )
                );
            }
        }

        return markers;
    }


}
