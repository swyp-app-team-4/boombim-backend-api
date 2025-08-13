package boombimapi.domain.vote.presentation.dto.res;

import boombimapi.domain.vote.presentation.dto.res.list.MyVoteRes;
import boombimapi.domain.vote.presentation.dto.res.list.VoteRes;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "투표 목록 응답 DTO")
public record VoteListRes(

        @ArraySchema(schema = @Schema(description = "반경 500m 이내의 투표 목록", implementation = VoteRes.class))
        List<VoteRes> voteResList,

        @ArraySchema(schema = @Schema(description = "내 질문(내가 만든 투표 + 중복 등록된 투표) 목록", implementation = MyVoteRes.class))
        List<MyVoteRes> myVoteResList
) {
    public static VoteListRes of(List<VoteRes> voteResList,
                                 List<MyVoteRes> myVoteResList) {
        return new VoteListRes(voteResList, myVoteResList);
    }
}
