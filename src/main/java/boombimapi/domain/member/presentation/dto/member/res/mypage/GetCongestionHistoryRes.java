package boombimapi.domain.member.presentation.dto.member.res.mypage;

import boombimapi.domain.congestion.entity.MemberCongestion;
import java.time.LocalDateTime;

public record GetCongestionHistoryRes(

        String posName,

        String congestionLevel,

        LocalDateTime createdAt

) {

    public static GetCongestionHistoryRes of(String posName, MemberCongestion memberCongestion) {
        return new GetCongestionHistoryRes(posName, memberCongestion.getCongestionLevel().getName(),
                memberCongestion.getCreatedAt());
    }
}
