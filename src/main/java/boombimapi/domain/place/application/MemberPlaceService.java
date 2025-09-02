package boombimapi.domain.place.application;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.place.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.member.ResolveMemberPlaceResponse;
import boombimapi.domain.place.dto.response.node.ViewportClusterNodeResponse;
import boombimapi.domain.place.dto.response.node.ViewportNodeResponse;
import boombimapi.domain.place.dto.response.node.ViewportPlaceNodeResponse;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.dto.Coordinate;
import boombimapi.global.geo.core.ClusterMarker;
import boombimapi.global.geo.core.ClusterPoint;
import boombimapi.global.geo.core.Clusterer;
import boombimapi.global.geo.internal.GeoDistance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final MemberPlaceRepository memberPlaceRepository;
    private final MemberCongestionRepository memberCongestionRepository;

    private final Clusterer clusterer;

    public ResolveMemberPlaceResponse resolveMemberPlace(
            ResolveMemberPlaceRequest request
    ) {
        log.info("[MemberPlaceService] resolveMemberPlace()");


        // TODO: 레이스 컨디션 처리 필요(동일 uuid 동시 생성 방지)
        MemberPlace memberPlace = memberPlaceRepository.findByUuid(request.uuid())
                .orElseGet(() -> memberPlaceRepository.save(
                        MemberPlace.of(
                                request.uuid(),
                                request.name(),
                                request.latitude(),
                                request.longitude(),
                                request.imageUrl()
                        )
                ));

        return ResolveMemberPlaceResponse.from(memberPlace);
    }

    public List<ViewportNodeResponse> getViewportNodes(
            ViewportRequest request
    ) {

        // 1) 뷰포트 경계 계산
        double lat1 = request.topLeft().latitude();
        double lng1 = request.topLeft().longitude();
        double lat2 = request.bottomRight().latitude();
        double lng2 = request.bottomRight().longitude();

        double minLatitude = Math.min(lat1, lat2);
        double maxLatitude = Math.max(lat1, lat2);
        double minLongitude = Math.min(lng1, lng2);
        double maxLongitude = Math.max(lng1, lng2);

        // 2) 뷰포트 내 장소 조회
        List<MemberPlace> allPlaces = memberPlaceRepository
                .findByLatitudeBetweenAndLongitudeBetween(minLatitude, maxLatitude, minLongitude, maxLongitude);

        // 3) 유효 혼잡도 존재 장소만 필터링
        LocalDateTime now = LocalDateTime.now();
        List<ClusterPoint> clusterPoints = new ArrayList<>();
        Map<Long, MemberPlace> memberPlaceMap = new HashMap<>(allPlaces.size());

        for (MemberPlace memberPlace : allPlaces) {
            boolean exists = memberCongestionRepository
                    .existsByMemberPlaceIdAndExpiresAtAfter(memberPlace.getId(), now);
            if (exists) {
                clusterPoints.add(new ClusterPoint(memberPlace.getId(), memberPlace.getLatitude(), memberPlace.getLongitude()));
                memberPlaceMap.put(memberPlace.getId(), memberPlace);
            }
        }

        if (clusterPoints.isEmpty()) {
            return List.of();
        }

        int zoomLevel = request.zoomLevel();

        // Clustering
        List<ClusterMarker> clusterMarkers = clusterer.cluster(
                clusterPoints,
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude,
                zoomLevel
        );

        int minClusterSize = 2;
        int detailZoom = 20;
        boolean highZoom = zoomLevel >= detailZoom;

        double memberLatitude = request.memberCoordinate().latitude();
        double memberLongitude = request.memberCoordinate().longitude();

        List<ViewportNodeResponse> nodes = new ArrayList<>(clusterMarkers.size());

        for (ClusterMarker clusterMarker : clusterMarkers) {
            // (A) 클러스터 모드
            if (!highZoom && clusterMarker.count() >= minClusterSize) {
                Map<String, Integer> levelCounts = new HashMap<>();

                for (Long placeId : clusterMarker.memberPlaceIds()) {
                    Optional<MemberCongestion> optionalMemberCongestion =
                            memberCongestionRepository.findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
                                    placeId, now
                            );

                    if (optionalMemberCongestion.isEmpty())
                        continue;

                    String levelName = optionalMemberCongestion.get().getCongestionLevel().getName();
                    levelCounts.merge(levelName, 1, Integer::sum);
                }

                nodes.add(
                        ViewportClusterNodeResponse.of(
                                new Coordinate(clusterMarker.latitude(), clusterMarker.longitude()),
                                clusterMarker.count(),
                                levelCounts
                        )
                );

                continue;
            }

            for (Long placeId : clusterMarker.memberPlaceIds()) {
                MemberPlace memberPlace = memberPlaceMap.get(placeId);

                if (memberPlace == null)
                    continue;

                Optional<MemberCongestion> optionalMemberCongestion =
                        memberCongestionRepository.findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
                                placeId, now
                        );

                if (optionalMemberCongestion.isEmpty())
                    continue;

                MemberCongestion memberCongestion = optionalMemberCongestion.get();

                double distanceMeters = GeoDistance.haversineMeters(
                        memberLatitude,
                        memberLongitude,
                        memberPlace.getLatitude(),
                        memberPlace.getLongitude()
                );

                nodes.add(
                        ViewportPlaceNodeResponse.of(
                                memberPlace.getId(),
                                memberPlace.getName(),
                                new Coordinate(memberPlace.getLatitude(), memberPlace.getLongitude()),
                                distanceMeters,
                                memberCongestion.getCongestionLevel().getName(),
                                memberCongestion.getCongestionMessage()
                        )
                );
            }
        }

        return nodes;
    }

}
