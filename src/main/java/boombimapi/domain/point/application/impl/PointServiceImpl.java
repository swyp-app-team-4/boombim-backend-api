package boombimapi.domain.point.application.impl;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.point.application.PointService;
import boombimapi.domain.point.domain.entity.Point;
import boombimapi.domain.point.domain.entity.PointHistory;
import boombimapi.domain.point.domain.entity.type.PointAction;
import boombimapi.domain.point.domain.entity.type.PointCategory;
import boombimapi.domain.point.domain.repository.PointHistoryRepository;
import boombimapi.domain.point.domain.repository.PointRepository;
import boombimapi.domain.point.presentation.dto.res.GetPointHistoryRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;

    @Override
    public void earnPointForCongestion(Member member, Long balance) {
        Point point = pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));


        point.addBalance(balance);

        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(balance)
                .balance(point.getBalance())
                .pointCategory(PointCategory.CONGESTION)
                .pointAction(PointAction.EARN)
                .build();

        pointHistoryRepository.save(pointHistory);
    }

    @Override
    public List<GetPointHistoryRes> getPointHistory(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        List<PointHistory> pointHistories = pointHistoryRepository.findAllByMemberOrderByCreatedAtDesc(member);

        List<GetPointHistoryRes> result = new ArrayList<>();

        for (PointHistory pointHistory : pointHistories) {
            result.add(GetPointHistoryRes.of(pointHistory));
        }


        return result;
    }

    @Override
    public void usePointForEvent(String memberId, Long balance) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        Point point = pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));


        point.subtractBalance(balance);

        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(balance)
                .balance(point.getBalance())
                .pointCategory(PointCategory.EVENT)
                .pointAction(PointAction.USE)
                .build();

        pointHistoryRepository.save(pointHistory);
    }
}
