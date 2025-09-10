package boombimapi.domain.member.presentation.dto.member.res;

import boombimapi.domain.member.presentation.dto.member.res.mypage.MPVoteRes;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "마이페이지 투표 응답 (날짜별 그룹핑)")
public record MyPageVoteRes(

        @Schema(description = "투표 날짜 (해당 일자의 시작 시간)", example = "2025-07-15T00:00:00")
        LocalDateTime day,

        @Schema(description = "해당 날짜의 투표 리스트")
        List<MPVoteRes> res
) {
    public static MyPageVoteRes of(LocalDateTime day, List<MPVoteRes> res) {
        return new MyPageVoteRes(day, res);
    }
}
