package boombimapi.domain.point.application.impl;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.point.application.PointService;
import boombimapi.domain.point.domain.entity.EventCampaign;
import boombimapi.domain.point.domain.entity.EventLog;
import boombimapi.domain.point.domain.entity.Point;
import boombimapi.domain.point.domain.entity.PointHistory;
import boombimapi.domain.point.domain.entity.type.EventCategory;
import boombimapi.domain.point.domain.entity.type.PointAction;
import boombimapi.domain.point.domain.entity.type.PointCategory;
import boombimapi.domain.point.domain.repository.EventCampaignRepository;
import boombimapi.domain.point.domain.repository.EventLogRepository;
import boombimapi.domain.point.domain.repository.PointHistoryRepository;
import boombimapi.domain.point.domain.repository.PointRepository;
import boombimapi.domain.point.presentation.dto.req.UsePointForEventReq;
import boombimapi.domain.point.presentation.dto.res.EventPageRes;
import boombimapi.domain.point.presentation.dto.res.GetPointHistoryRes;
import boombimapi.domain.point.presentation.dto.res.GetPointRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

/**
 * PointServiceImpl 포인트 적립, 조회, 사용(이벤트 응모) 관련 비즈니스 로직을 담당한다. - 혼잡도 작성 시 포인트 적립 - 포인트 및 이력 조회 - 이벤트 응모 시 포인트 차감 및 응모 이력
 * 생성
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
     * [혼잡도 작성 시 포인트 적립] - 특정 회원의 포인트 잔액을 증가시키고, 적립 이력을 저장한다. - 포인트가 존재하지 않으면 예외 발생.
     *
     * @param member  포인트를 적립할 회원
     * @param balance 적립할 포인트 금액
     */
    @Override
    public void earnPointForCongestion(Member member, Long balance) {
        Point point = pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));

        // 포인트 적립
        point.addBalance(balance);

        // 포인트 이력 생성
        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(balance)
                .balance(point.getBalance())
                .pointCategory(PointCategory.CONGESTION)
                .pointAction(PointAction.EARN)
                .build();

        pointHistoryRepository.save(pointHistory);
    }

    /**
     * [회원 포인트 및 이력 조회] - 회원의 현재 포인트 잔액과 포인트 거래 이력을 조회한다. - 회원 또는 포인트 정보가 존재하지 않으면 예외 발생.
     *
     * @param memberId 조회할 회원 ID
     * @return 포인트 잔액 및 거래 이력 응답 DTO
     */
    @Override
    public GetPointRes getPointHistory(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        Point point = pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));

        List<PointHistory> pointHistories = pointHistoryRepository.findAllByMemberOrderByCreatedAtDesc(member);
        List<GetPointHistoryRes> result = new ArrayList<>();

        for (PointHistory pointHistory : pointHistories) {
            result.add(GetPointHistoryRes.of(pointHistory));
        }

        return GetPointRes.of(point.getBalance(), result);
    }

    /**
     * [이벤트 응모 시 포인트 차감] - 회원 포인트 잔액에서 지정 금액을 차감하고, 응모 이력을 저장한다. - 응모 횟수가 5회를 초과하면 예외 발생. - 포인트가 부족한 경우 예외 발생.
     *
     * @param memberId 회원 ID
     * @param req      이벤트 응모 요청 (이벤트 캠페인 ID 및 차감할 포인트 금액 포함)
     */
    @Override
    public void usePointForEvent(String memberId, UsePointForEventReq req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        Point point = pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));

        EventCampaign eventCampaign = eventCampaignRepository.findById(req.eventCampaignId())
                .orElseThrow(() -> new BoombimException(EVENT_NOT_EXIST));

        // 포인트 잔액 부족 예외
        if (point.getBalance() - req.amount() < 0) {
            throw new BoombimException(INSUFFICIENT_POINT_FOR_EVENT);
        }

        // 이벤트 응모 제한 초과 예외
        if (point.getApplyEventCount() == 5) {
            throw new BoombimException(EVENT_PARTICIPATION_LIMIT_EXCEEDED);
        }

        // 포인트 차감 및 응모 횟수 증가
        point.subtractBalance(req.amount());
        point.addApplyEventCnt();

        // 이벤트 응모 이력 저장
        EventLog eventLog = EventLog.builder()
                .member(member)
                .eventCampaign(eventCampaign)
                .build();

        eventLogRepository.save(eventLog);

        // 포인트 거래 이력 저장
        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(req.amount())
                .balance(point.getBalance())
                .pointCategory(PointCategory.EVENT)
                .pointAction(PointAction.USE)
                .build();

        pointHistoryRepository.save(pointHistory);
    }

    @Override
    public EventPageRes getOngoingEventPage() {
        EventCampaign eventCampaign = eventCampaignRepository.findTopByOrderByCreatedAtDesc().orElse(null);
        if(eventCampaign == null) throw new BoombimException(EVENT_NOT_EXIST);

        return EventPageRes.of(eventCampaign);
    }
}
