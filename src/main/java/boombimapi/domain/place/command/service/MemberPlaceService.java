package boombimapi.domain.place.command.service;

import static boombimapi.domain.place.shared.type.PlaceType.MEMBER_PLACE;
import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.dto.response.MemberCongestionItemResponse;
import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.place.cluster.WebMercator;
import boombimapi.domain.place.command.api.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.command.infrastructure.NaverStaticMapWebClient;
import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.member.GetMemberPlaceDetailResponse;
import boombimapi.domain.place.query.api.dto.response.member.MemberPlaceSummaryResponse;
import boombimapi.domain.place.command.api.dto.response.ResolveMemberPlaceResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportClusterMarkerResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportMarkerResponse;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportPlaceMarkerResponse;
import boombimapi.domain.place.command.entity.MemberPlace;
import boombimapi.domain.place.command.repository.MemberPlaceRepository;
import boombimapi.global.infra.s3.presentation.application.S3Service;
import boombimapi.global.properties.ClusterProperties;
import boombimapi.global.vo.Coordinate;
import boombimapi.domain.place.cluster.vo.ClusterResult;
import boombimapi.domain.place.cluster.vo.ClusterInput;
import boombimapi.domain.place.cluster.Clusterer;
import boombimapi.global.geo.GeoDistance;

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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final MemberPlaceRepository memberPlaceRepository;
    private final MemberCongestionRepository memberCongestionRepository;

    private final FavoriteRepository favoriteRepository;

    private final NaverStaticMapWebClient naverStaticMapWebClient;
    private final S3Service s3Service;

    // TODO: 추후 트랜잭션 분리 시 리팩터링
    @Transactional
    public ResolveMemberPlaceResponse resolveMemberPlace(
        ResolveMemberPlaceRequest request
    ) {
        Optional<MemberPlace> memberPlaceOptional = memberPlaceRepository.findByUuid(request.uuid());

        if (memberPlaceOptional.isPresent()) {
            return ResolveMemberPlaceResponse.from(memberPlaceOptional.get());
        }

        MemberPlace created = memberPlaceRepository.save(
            MemberPlace.of(
                request.uuid(),
                request.name(),
                request.address(),
                request.latitude(),
                request.longitude()
            )
        );

        try {
            byte[] bytes = naverStaticMapWebClient.fetchStaticMapImage(request.latitude(), request.longitude());
            String key = "maps/naver/static-map/%d.png".formatted(created.getId());
            String url = s3Service.storeStaticMapImage(key, bytes, "image/png");
            memberPlaceRepository.updateImageUrl(created.getId(), url);
        } catch (Exception e) {
            log.warn("Static map generation failed. placeId={}, err={}", created.getId(), e.toString());
        }

        return ResolveMemberPlaceResponse.from(created);
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

    public List<ViewportMarkerResponse> getViewportMarkers(
        String memberId,
        ViewportRequest request,
        Clusterer clusterer,
        ClusterProperties properties
    ) {

        // 1) 뷰포트 경계 계산
        // TODO: 추출 필요
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
        List<MemberPlace> validPlaces = new ArrayList<>(allPlaces.size());
        for (MemberPlace memberPlace : allPlaces) {
            if (memberCongestionRepository.existsByMemberPlaceIdAndExpiresAtAfter(memberPlace.getId(), now)) {
                validPlaces.add(memberPlace);
            }
        }
        if (validPlaces.isEmpty()) {
            return List.of();
        }

        int zoomLevel = request.zoomLevel();
        boolean isMaxZoom = zoomLevel == properties.maxZoomAtRefZ();
        double memberLatitude = request.memberCoordinate().latitude();
        double memberLongitude = request.memberCoordinate().longitude();

        if (isMaxZoom) {
            List<ViewportMarkerResponse> markers = new ArrayList<>(validPlaces.size());

            for (MemberPlace memberPlace : validPlaces) {
                Optional<MemberCongestion> memberCongestionOptional = memberCongestionRepository
                    .findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(memberPlace.getId(), now);
                if (memberCongestionOptional.isEmpty()) {
                    continue;
                }

                MemberCongestion memberCongestion = memberCongestionOptional.get();
                Double memberPlaceLatitude = memberPlace.getLatitude();
                Double memberPlaceLongitude = memberPlace.getLongitude();

                double distanceMeters = GeoDistance.haversineMeters(
                    memberLatitude,
                    memberLongitude,
                    memberPlaceLatitude,
                    memberPlaceLongitude
                );

                boolean isFavorite = isFavorite(memberId, memberPlace.getId());

                markers.add(
                    ViewportPlaceMarkerResponse.of(
                        memberPlace.getId(),
                        memberPlace.getName(),
                        new Coordinate(memberPlaceLatitude, memberPlaceLongitude),
                        distanceMeters,
                        memberCongestion.getCongestionLevel().getName(),
                        memberCongestion.getCongestionMessage(),
                        memberCongestion.getCreatedAt(),
                        isFavorite
                    )
                );

            }

            return markers;
        }

        List<ClusterInput> clusterInputs = new ArrayList<>(validPlaces.size());

        for (MemberPlace memberPlace : validPlaces) {
            ClusterInput clusterInput = new ClusterInput(
                memberPlace.getId(),
                memberPlace.getLatitude(),
                memberPlace.getLongitude()
            );
            clusterInputs.add(clusterInput);
        }

        List<ClusterResult> clusterResults = clusterer.cluster(clusterInputs, zoomLevel);

        Map<Long, MemberPlace> memberPlaceMap = new HashMap<>(validPlaces.size());
        for (MemberPlace memberPlace : validPlaces) {
            memberPlaceMap.put(memberPlace.getId(), memberPlace);
        }

        final int minClusterSize = properties.minClusterSize();
        final int refZ = properties.refZ();
        final double tileSize = properties.tileSize();

        List<ViewportMarkerResponse> markers = new ArrayList<>(clusterResults.size());
        for (ClusterResult clusterResult : clusterResults) {
            if (clusterResult.count() >= minClusterSize) {
                double longitude = WebMercator.worldPixelXToLongitude(
                    clusterResult.centroidWorldPixelX(),
                    refZ,
                    tileSize
                );

                double latitude = WebMercator.worldPixelYToLatitude(
                    clusterResult.centroidWorldPixelY(),
                    refZ,
                    tileSize
                );

                Map<String, Integer> levelCounts = new HashMap<>();

                for (Long placeId : clusterResult.placeIds()) {
                    Optional<MemberCongestion> memberCongestionOptional =
                        memberCongestionRepository.findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
                            placeId, now
                        );

                    if (memberCongestionOptional.isEmpty())
                        continue;

                    String levelName = memberCongestionOptional.get().getCongestionLevel().getName();
                    levelCounts.merge(levelName, 1, Integer::sum);
                }

                markers.add(
                    ViewportClusterMarkerResponse.of(
                        new Coordinate(latitude, longitude),
                        clusterResult.count(),
                        levelCounts
                    )
                );

                continue;
            }

            for (Long placeId : clusterResult.placeIds()) {
                MemberPlace memberPlace = memberPlaceMap.get(placeId);
                if (memberPlace == null) {
                    continue;
                }

                Optional<MemberCongestion> memberCongestionOptional = memberCongestionRepository
                    .findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(placeId, now);

                if (memberCongestionOptional.isEmpty()) {
                    continue;
                }

                MemberCongestion memberCongestion = memberCongestionOptional.get();

                double distanceMeters = GeoDistance.haversineMeters(
                    memberLatitude,
                    memberLongitude,
                    memberPlace.getLatitude(),
                    memberPlace.getLongitude()
                );

                boolean isFavorite = isFavorite(memberId, memberPlace.getId());

                markers.add(
                    ViewportPlaceMarkerResponse.of(
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

        return markers;
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
