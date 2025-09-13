package boombimapi.domain.place.application;

import static boombimapi.domain.place.entity.PlaceType.*;
import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.dto.response.MemberCongestionItemResponse;
import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.member.GetMemberPlaceDetailResponse;
import boombimapi.domain.place.dto.response.member.MemberPlaceSummaryResponse;
import boombimapi.domain.place.dto.response.member.ResolveMemberPlaceResponse;
import boombimapi.domain.place.dto.response.node.ViewportClusterNodeResponse;
import boombimapi.domain.place.dto.response.node.ViewportNodeResponse;
import boombimapi.domain.place.dto.response.node.ViewportPlaceNodeResponse;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.vo.Coordinate;
import boombimapi.global.geo.core.ClusterMarker;
import boombimapi.global.geo.core.ClusterPoint;
import boombimapi.global.geo.core.Clusterer;
import boombimapi.global.geo.internal.GeoDistance;

import boombimapi.global.infra.exception.error.BoombimException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final MemberRepository memberRepository;
    private final MemberPlaceRepository memberPlaceRepository;
    private final MemberCongestionRepository memberCongestionRepository;

    private final Clusterer clusterer;
    private final FavoriteRepository favoriteRepository;

    public ResolveMemberPlaceResponse resolveMemberPlace(
        ResolveMemberPlaceRequest request
    ) {

        // TODO: 레이스 컨디션 처리 필요(동일 uuid 동시 생성 방지)
        MemberPlace memberPlace = memberPlaceRepository.findByUuid(request.uuid())
            .orElseGet(() -> memberPlaceRepository.save(
                MemberPlace.of(
                    request.uuid(),
                    request.name(),
                    request.address(),
                    request.latitude(),
                    request.longitude(),
                    request.imageUrl()
                )
            ));

        return ResolveMemberPlaceResponse.from(memberPlace);
    }

    public GetMemberPlaceDetailResponse getMemberPlaceDetail(
        String memberId,
        Long memberPlaceId,
        Integer size,
        Long cursor
    ) {
        int pageSize = sanitizeSize(size);
        Pageable pageable = PageRequest.of(0, pageSize);

        MemberPlace memberPlace = memberPlaceRepository.findById(memberPlaceId)
            .orElseThrow(() -> new BoombimException(MEMBER_PLACE_NOT_FOUND));

        Slice<MemberCongestion> slice = loadMemberCongestionSlice(
            memberPlaceId,
            cursor,
            pageable
        );

        // TODO: N+1 발생 가능성 높음
        List<MemberCongestionItemResponse> memberCongestionItems = slice.getContent()
            .stream()
            .map(memberCongestion -> {
                Member member = memberCongestion.getMember();

                return MemberCongestionItemResponse.of(
                    memberCongestion.getId(),
                    member.getProfile(),
                    member.getName(),
                    memberCongestion.getCongestionLevel().getName(),
                    memberCongestion.getCongestionMessage(),
                    memberCongestion.getCreatedAt()
                );
            })
            .toList();

        Long nextCursor = computeNextCursor(memberCongestionItems);

        boolean isFavorite = isFavorite(memberId, memberPlaceId);

        MemberPlaceSummaryResponse memberPlaceSummary = MemberPlaceSummaryResponse.of(
            memberPlace.getId(),
            memberPlace.getName(),
            memberPlace.getAddress(),
            memberPlace.getLatitude(),
            memberPlace.getLongitude(),
            memberPlace.getImageUrl(),
            isFavorite
        );

        return GetMemberPlaceDetailResponse.of(
            memberPlaceSummary,
            memberCongestionItems,
            slice.hasNext(),
            nextCursor,
            pageSize
        );
    }

    public List<ViewportNodeResponse> getViewportNodes(
        String memberId,
        ViewportRequest request
    ) {

        log.info("[MemberPlaceService] getViewportNodes() zoomLevel: {}", request.zoomLevel());

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

                boolean isFavorite = isFavorite(memberId, memberPlace.getId());

                nodes.add(
                    ViewportPlaceNodeResponse.of(
                        memberPlace.getId(),
                        memberPlace.getName(),
                        new Coordinate(memberPlace.getLatitude(), memberPlace.getLongitude()),
                        distanceMeters,
                        memberCongestion.getCongestionLevel().getName(),
                        memberCongestion.getCongestionMessage(),
                        memberCongestion.getCreatedAt(),
                        isFavorite
                    )
                );

            }
        }

        return nodes;
    }

    private int sanitizeSize(
        Integer size
    ) {
        if (size == null || size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private Long computeNextCursor(
        List<MemberCongestionItemResponse> memberCongestionItems
    ) {
        if (memberCongestionItems == null || memberCongestionItems.isEmpty()) {
            return null;
        }

        int size = memberCongestionItems.size();

        return memberCongestionItems.get(size - 1).memberCongestionId();
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
            MEMBER_PLACE
        );
    }

    private Slice<MemberCongestion> loadMemberCongestionSlice(
        Long memberPlaceId,
        Long cursor,
        Pageable pageable
    ) {
        if (cursor == null) {
            return loadInitialSlice(memberPlaceId, pageable);
        }
        return loadNextSlice(memberPlaceId, cursor, pageable);
    }

    private Slice<MemberCongestion> loadInitialSlice(
        Long memberPlaceId,
        Pageable pageable
    ) {
        return memberCongestionRepository.findByMemberPlaceIdOrderByIdDesc(
            memberPlaceId,
            pageable
        );
    }

    private Slice<MemberCongestion> loadNextSlice(
        Long memberPlaceId,
        Long cursor,
        Pageable pageable
    ) {
        return memberCongestionRepository.findByMemberPlaceIdAndIdLessThanOrderByIdDesc(
            memberPlaceId,
            cursor,
            pageable
        );
    }

}
