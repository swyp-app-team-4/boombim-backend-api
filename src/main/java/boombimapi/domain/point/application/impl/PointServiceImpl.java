package boombimapi.domain.point.application.impl;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.point.application.PointService;
import boombimapi.domain.point.domain.entity.*;
import boombimapi.domain.point.domain.entity.type.*;
import boombimapi.domain.point.domain.repository.*;
import boombimapi.domain.point.presentation.dto.req.*;
import boombimapi.domain.point.presentation.dto.res.*;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

/**
 * PointServiceImpl - 포인트 적립, 사용(이벤트 응모), 조회 등 주요 포인트 관련 비즈니스 로직을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;
    private final EventLogRepository eventLogRepository;
    private final EventCampaignRepository eventCampaignRepository;

    /**
     * 혼잡도 작성 시 포인트 적립 - 회원의 포인트 잔액을 증가시키고 이력을 남긴다.
     */
    @Override
    public void earnPointForCongestion(Member member, Long balance) {
        Point point = getPointByMember(member);
        point.addBalance(balance);
        pointHistoryRepository.save(createHistory(member, balance, point, PointCategory.CONGESTION, PointAction.EARN));
    }

    /**
     * 회원 포인트 및 이력 조회 - 회원 ID 기준으로 포인트 잔액과 거래 내역을 반환한다.
     */
    @Override
    public GetPointRes getPointHistory(String memberId) {
        Member member = getMember(memberId);
        Point point = getPointByMember(member);
        List<GetPointHistoryRes> histories = pointHistoryRepository.findAllByMemberOrderByCreatedAtDesc(member)
                .stream().map(GetPointHistoryRes::of).toList();
        return GetPointRes.of(point.getBalance(), histories);
    }

    /**
     * 이벤트 응모 시 포인트 차감 - 응모 제한과 잔액을 검증 후 포인트 차감 및 응모 로그 저장.
     */
    @Override
    public void usePointForEvent(String memberId, UsePointForEventReq req) {
        Member member = getMember(memberId);
        Point point = getPointByMember(member);
        EventCampaign event = getEventCampaign(req.eventCampaignId());
        validateEventParticipation(point, req.amount());
        applyEvent(point, req.amount());
        eventLogRepository.save(EventLog.builder().member(member).eventCampaign(event).build());
        pointHistoryRepository.save(createHistory(member, req.amount(), point, PointCategory.EVENT, PointAction.USE));
    }

    /**
     * 진행 중인 이벤트 페이지 조회 - 최신 등록된 이벤트 정보를 반환한다.
     */
    @Override
    public EventPageRes getOngoingEventPage(String userId) {
        Member member = getMember(userId);

        Point point = pointRepository.findByMember(member).orElse(null);
        if (point == null) {
            throw new BoombimException(POINT_NOT_EXIST);
        }

        EventCampaign eventCampaign = eventCampaignRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new BoombimException(EVENT_NOT_EXIST));

        List<EventLog> byEventLog = eventLogRepository.findByMemberAndEventCampaign(member, eventCampaign);

        return EventPageRes.of(eventCampaign, point.getBalance(), (long) byEventLog.size());
    }

    /**
     * 새로운 이벤트 캠페인 생성 - 이벤트 시작, 종료, 발표일, 카테고리를 포함하여 저장한다.
     */
    @Override
    public void createEventCampaign(CreateEventCampaignReq req) {
        eventCampaignRepository.save(EventCampaign.builder()
                .eventStartDate(req.eventStartDate())
                .eventEndDate(req.eventEndDate())
                .winnerAnnouncementDate(req.winnerAnnouncementDate())
                .eventCategory(req.eventCategory())
                .build());
    }

    /* ===== Private Helper Methods ===== */

    /**
     * 회원 ID로 회원 조회 (없으면 예외 발생)
     */
    private Member getMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));
    }

    /**
     * 회원 기준 포인트 조회 (없으면 예외 발생)
     */
    private Point getPointByMember(Member member) {
        return pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));
    }

    /**
     * 이벤트 ID로 캠페인 조회 (없으면 예외 발생)
     */
    private EventCampaign getEventCampaign(Long id) {
        return eventCampaignRepository.findById(id)
                .orElseThrow(() -> new BoombimException(EVENT_NOT_EXIST));
    }

    /**
     * 포인트 거래 이력 엔티티 생성
     */
    private PointHistory createHistory(Member m, Long amt, Point p, PointCategory c, PointAction a) {
        return PointHistory.builder()
                .member(m)
                .amount(amt)
                .balance(p.getBalance())
                .pointCategory(c)
                .pointAction(a)
                .build();
    }

    /**
     * 이벤트 응모 제한 및 잔액 검증
     */
    private void validateEventParticipation(Point point, Long amount) {
        if (point.getBalance() < amount) {
            throw new BoombimException(INSUFFICIENT_POINT_FOR_EVENT);
        }
        if (point.getApplyEventCount() >= 5) {
            throw new BoombimException(EVENT_PARTICIPATION_LIMIT_EXCEEDED);
        }
    }

    /**
     * 이벤트 응모 처리 (포인트 차감 + 응모 횟수 증가)
     */
    private void applyEvent(Point point, Long amount) {
        point.subtractBalance(amount);
        point.addApplyEventCnt();
    }
}
