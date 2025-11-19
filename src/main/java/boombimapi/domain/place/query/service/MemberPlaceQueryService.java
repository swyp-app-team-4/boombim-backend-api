package boombimapi.domain.place.query.service;

import boombimapi.domain.place.cluster.Clusterer;
import boombimapi.domain.place.cluster.WebMercator;
import boombimapi.domain.place.cluster.vo.ClusterInput;
import boombimapi.domain.place.cluster.vo.ClusterResult;
import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportClusterMarkerResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportMarkerResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportPlaceMarkerResponse;
import boombimapi.domain.place.query.dao.MemberPlaceQueryDao;
import boombimapi.domain.place.query.dao.param.ViewportParam;
import boombimapi.domain.place.query.dao.row.MemberPlaceViewportRow;
import boombimapi.domain.place.shared.type.PlaceType;
import boombimapi.global.properties.ClusterProperties;
import boombimapi.global.vo.Coordinate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class MemberPlaceQueryService {

    private final MemberPlaceQueryDao memberPlaceQueryDao;

    public List<ViewportMarkerResponse> getMemberPlacesInViewport(
        String memberId,
        ViewportRequest request,
        Clusterer clusterer,
        ClusterProperties properties
    ) {
        ViewportParam viewportParam = ViewportParam.from(memberId, request);

        List<MemberPlaceViewportRow> rows = memberPlaceQueryDao.findInViewPort(viewportParam);

        if (rows.isEmpty()) {
            return List.of();
        }

        final int zoomLevel = request.zoomLevel();
        final boolean isMaxZoom = zoomLevel == properties.maxZoomAtRefZ();
        final LocalDateTime now = LocalDateTime.now();

        // 1. 최대 줌 레벨에서는 클러스터링 X
        if (isMaxZoom) {
            List<ViewportMarkerResponse> markers = new ArrayList<>(rows.size());
            for (MemberPlaceViewportRow row : rows) {

                Boolean isExpired = resolveIsExpired(row, now);

                markers.add(
                    ViewportPlaceMarkerResponse.of(
                        row.id(),
                        row.name(),
                        PlaceType.MEMBER_PLACE,
                        Coordinate.of(row.latitude(), row.longitude()),
                        null,
                        row.congestionLevelName(),
                        row.congestionMessage(),
                        row.createdAt(),
                        row.isFavorite(),
                        isExpired
                    )
                );
            }

            return markers;
        }

        // 2. 클러스터링을 위한 변환
        List<ClusterInput> inputs = new ArrayList<>(rows.size());
        for (MemberPlaceViewportRow row : rows) {
            inputs.add(new ClusterInput(
                row.id(),
                row.latitude(),
                row.longitude()
            ));
        }

        // 3. 클러스터 실행
        List<ClusterResult> clusterResults = clusterer.cluster(inputs, zoomLevel);

        Map<Long, MemberPlaceViewportRow> rowHashMap = new HashMap<>(rows.size());
        for (MemberPlaceViewportRow row : rows) {
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
                    MemberPlaceViewportRow row = rowHashMap.get(placeId);

                    if (row == null)
                        continue;

                    Boolean isExpired = resolveIsExpired(row, now);
                    if (Boolean.TRUE.equals(isExpired)) {
                        continue;
                    }

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
                MemberPlaceViewportRow row = rowHashMap.get(placeId);
                if (row == null)
                    continue;

                Boolean isExpired = resolveIsExpired(row, now);

                markers.add(
                    ViewportPlaceMarkerResponse.of(
                        row.id(),
                        row.name(),
                        PlaceType.MEMBER_PLACE,
                        Coordinate.of(row.latitude(), row.longitude()),
                        null,
                        row.congestionLevelName(),
                        row.congestionMessage(),
                        row.createdAt(),
                        row.isFavorite(),
                        isExpired
                    )
                );
            }
        }

        return markers;
    }

    private Boolean resolveIsExpired(
        MemberPlaceViewportRow row,
        LocalDateTime now
    ) {
        if (row.expiresAt() == null) {
            return Boolean.TRUE;
        }

        return row.expiresAt().isBefore(now);
    }
}
