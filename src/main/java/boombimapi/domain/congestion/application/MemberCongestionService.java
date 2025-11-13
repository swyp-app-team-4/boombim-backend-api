package boombimapi.domain.congestion.application;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.dto.request.CreateMemberCongestionRequest;
import boombimapi.domain.congestion.dto.response.CreateMemberCongestionResponse;
import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.CongestionLevelRepository;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.command.entity.MemberPlace;
import boombimapi.domain.place.command.repository.MemberPlaceRepository;
import boombimapi.domain.point.application.PointService;
import boombimapi.domain.point.infrastructure.repository.MemberPointGrantRepository;
import boombimapi.global.geo.GeoDistance;
import boombimapi.global.infra.exception.error.BoombimException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCongestionService {

    private final MemberRepository memberRepository;
    private final MemberPlaceRepository memberPlaceRepository;
    private final MemberCongestionRepository memberCongestionRepository;
    private final CongestionLevelRepository congestionLevelRepository;
    private final MemberPointGrantRepository memberPointGrantRepository;
    private final PointService pointService;

    public CreateMemberCongestionResponse createMemberCongestion(
        String memberId,
        CreateMemberCongestionRequest request
    ) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        MemberPlace memberPlace = memberPlaceRepository.findById(request.memberPlaceId())
            .orElseThrow(() -> new BoombimException(MEMBER_PLACE_NOT_FOUND));

        CongestionLevel congestionLevel = congestionLevelRepository.findById(request.congestionLevelId())
            .orElseThrow(() -> new BoombimException(CONGESTION_LEVEL_NOT_FOUND));

        String congestionMessage = request.congestionMessage();
        if (congestionMessage.isEmpty()) {
            congestionMessage = congestionLevel.getMessage();
        }

        boolean pointReceivable = checkPointReceivable(memberId, request.latitude(), request.longitude());

        if (pointReceivable) {
            pointService.earnPointForCongestion(member, 10L);
        }

        MemberCongestion memberCongestion = MemberCongestion.of(
            member,
            memberPlace,
            congestionLevel,
            congestionMessage,
            request.latitude(),
            request.longitude()
        );

        return CreateMemberCongestionResponse
            .of(memberCongestionRepository.save(memberCongestion), pointReceivable);
    }

    private boolean checkPointReceivable(
        String memberId,
        double latitude,
        double longitude
    ) {
        final int DAILY_LIMIT = 5;
        final long WINDOW_MS = 3_600_000L;

        int todayCount = memberPointGrantRepository.getTodayCount(memberId);
        if (todayCount >= DAILY_LIMIT) {
            return false;
        }

        long currentTimeMillis = System.currentTimeMillis();

        memberPointGrantRepository.removeStaledKeys(
            memberId,
            currentTimeMillis,
            WINDOW_MS
        );

        Set<String> recentCoordinates = memberPointGrantRepository.rangeByScore(
            memberId,
            currentTimeMillis - WINDOW_MS,
            currentTimeMillis
        );

        for (String coordinate : recentCoordinates) {
            int commaIndex = coordinate.indexOf(',');
            double prevLat = Double.parseDouble(coordinate.substring(0, commaIndex));
            double prevLon = Double.parseDouble(coordinate.substring(commaIndex + 1));
            double distance = GeoDistance.haversineMeters(latitude, longitude, prevLat, prevLon);
            if (distance <= 300.0) {
                return false;
            }
        }

        memberPointGrantRepository.addRecentCoordinate(
            memberId,
            latitude,
            longitude,
            currentTimeMillis
        );

        memberPointGrantRepository.incrementToday(memberId);

        return true;
    }
}
