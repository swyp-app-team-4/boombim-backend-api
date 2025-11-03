package boombimapi.domain.point.application;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.point.presentation.dto.req.UsePointForEventReq;
import boombimapi.domain.point.presentation.dto.res.EventPageRes;
import boombimapi.domain.point.presentation.dto.res.GetPointRes;

/**
 * PointService 포인트 관련 핵심 기능 정의 인터페이스.
 * <p>
 * - 혼잡도 작성 시 포인트 적립<br> - 포인트 및 이력 조회<br> - 이벤트 응모 시 포인트 차감
 */
public interface PointService {

    /**
     * 혼잡도 작성 시 포인트 적립.
     * <p>
     * 지정된 회원의 포인트 잔액을 증가시키고, 포인트 적립 이력을 기록한다.
     *
     * @param member  포인트를 적립할 회원 엔티티
     * @param balance 적립할 포인트 금액
     */
    void earnPointForCongestion(Member member, Long balance);

    /**
     * 회원 포인트 및 이력 조회.
     * <p>
     * 지정된 회원 ID로 현재 포인트 잔액과 거래 내역을 조회한다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 회원의 포인트 잔액 및 거래 이력 응답 DTO
     */
    GetPointRes getPointHistory(String memberId);

    /**
     * 이벤트 응모 시 포인트 차감.
     * <p>
     * 회원의 포인트 잔액에서 지정 금액을 차감하고, 응모 이력 및 포인트 사용 이력을 기록한다.
     *
     * @param memberId 회원 ID
     * @param req 이벤트 응모 요청 (이벤트 캠페인 ID 및 차감할 포인트 금액 포함)
     */
    void usePointForEvent(String memberId, UsePointForEventReq req);


    EventPageRes getOngoingEventPage();
}
