package boombimapi.domain.member.presentation.dto.res;

import boombimapi.domain.member.presentation.dto.res.mypage.MPVoteRes;

import java.time.LocalDateTime;
import java.util.List;

public record MyPageVoteRes(
        LocalDateTime day,

        List<MPVoteRes> res
) {
    public static MyPageVoteRes of(LocalDateTime day, List<MPVoteRes> res) {
        return new MyPageVoteRes(day, res);
    }
}

