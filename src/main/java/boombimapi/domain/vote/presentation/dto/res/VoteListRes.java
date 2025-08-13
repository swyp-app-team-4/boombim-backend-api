package boombimapi.domain.vote.presentation.dto.res;

import boombimapi.domain.vote.presentation.dto.res.list.MyVoteRes;
import boombimapi.domain.vote.presentation.dto.res.list.VoteRes;

import java.util.List;

public record VoteListRes(
        List<VoteRes> voteResList,

        List<MyVoteRes> myVoteResList
) {
    public static VoteListRes of(List<VoteRes> voteResList,
                                 List<MyVoteRes> myVoteResList) {
        return new VoteListRes(voteResList, myVoteResList);
    }
}
