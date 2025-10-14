package boombimapi.domain.point.application;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.point.presentation.dto.res.GetPointHistoryRes;

import java.util.List;

public interface PointService {

    //  혼잡도 작성 추가
    void earnPointForCongestion(Member member, Long balance);

    // 조회
    List<GetPointHistoryRes> getPointHistory(String memberId);

    // 이벤트 응모할때 20포인트 삭제
    void usePointForEvent(String memberId, Long balance);
}
