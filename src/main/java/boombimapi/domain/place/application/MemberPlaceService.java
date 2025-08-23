package boombimapi.domain.place.application;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.place.cluster.GridClusterer;
import boombimapi.domain.place.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.ResolveMemberPlaceResponse;
import boombimapi.domain.place.dto.response.node.ViewportClusterNodeResponse;
import boombimapi.domain.place.dto.response.node.ViewportNodeResponse;
import boombimapi.domain.place.dto.response.node.ViewportPlaceNodeResponse;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.dto.Coordinate;
import java.time.Instant;
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
                    request.longitude()
                )
            ));

        return ResolveMemberPlaceResponse.from(memberPlace);
    }

    public List<ViewportNodeResponse> getViewportNodes(ViewportRequest request) {

        // 1) 뷰포트 경계 계산
        double lat1 = request.topLeft().latitude();
        double lng1 = request.topLeft().longitude();
        double lat2 = request.bottomRight().latitude();
        double lng2 = request.bottomRight().longitude();

        double minLat = Math.min(lat1, lat2);
        double maxLat = Math.max(lat1, lat2);
        double minLng = Math.min(lng1, lng2);
        double maxLng = Math.max(lng1, lng2);

        // 2) 뷰포트 내 장소 조회
        List<MemberPlace> allPlaces = memberPlaceRepository
            .findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);

        // 3) 유효 혼잡도 존재 장소만 필터링
        Instant now = Instant.now();
        List<GridClusterer.Point> points = new ArrayList<>();
        Map<Long, MemberPlace> placeById = new HashMap<>(allPlaces.size());

        for (MemberPlace place : allPlaces) {
            boolean exists = memberCongestionRepository
                .existsByMemberPlaceIdAndExpiresAtAfter(place.getId(), now);
            if (exists) {
                points.add(new GridClusterer.Point(place.getId(), place.getLatitude(), place.getLongitude()));
                placeById.put(place.getId(), place);
            }
        }

        if (points.isEmpty()) {
            return List.of();
        }

        int zoom = request.zoom();

        // 4) 클러스터링
        List<GridClusterer.Bucket> buckets = GridClusterer.cluster(
            points, minLat, maxLat, minLng, maxLng, zoom
        );

        // 5) 분해 정책
        int minClusterSize = 2;
        int detailZoom = 20;
        boolean highZoom = zoom >= detailZoom;

        // 6) 응답 구성 (디테일로 푸는 경우에만 최신 혼잡도 조회)
        double memberLat = request.memberCoordinate().latitude();
        double memberLng = request.memberCoordinate().longitude();

        List<ViewportNodeResponse> nodes = new ArrayList<>(buckets.size());

        for (GridClusterer.Bucket bucket : buckets) {
            // (A) 클러스터 모드
            if (!highZoom && bucket.count() >= minClusterSize) {
                Map<String, Integer> levelCounts = new HashMap<>();

                for (Long placeId : bucket.placeIds()) {
                    Optional<MemberCongestion> mcOpt =
                        memberCongestionRepository.findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
                            placeId, now
                        );
                    if (mcOpt.isEmpty())
                        continue;

                    String levelName = mcOpt.get().getCongestionLevel().getName();
                    levelCounts.merge(levelName, 1, Integer::sum);
                }

                nodes.add(
                    ViewportClusterNodeResponse.of(
                        new Coordinate(bucket.lat(), bucket.lng()),
                        bucket.count(),
                        levelCounts
                    )
                );
                continue;
            }

            // (B) 디테일 모드: 버킷 내 장소들을 개별 마커로 전개
            for (Long placeId : bucket.placeIds()) {
                MemberPlace place = placeById.get(placeId);
                if (place == null)
                    continue;

                Optional<MemberCongestion> mcOpt =
                    memberCongestionRepository.findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
                        placeId, now
                    );
                if (mcOpt.isEmpty())
                    continue;

                MemberCongestion mc = mcOpt.get();

                double distanceMeters = haversine(
                    memberLat, memberLng, place.getLatitude(), place.getLongitude()
                );

                nodes.add(
                    ViewportPlaceNodeResponse.of(
                        place.getId(),
                        place.getName(),
                        new Coordinate(place.getLatitude(), place.getLongitude()),
                        distanceMeters,
                        mc.getCongestionLevel().getName(),
                        mc.getCongestionMessage()
                    )
                );
            }
        }

        return nodes;
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
