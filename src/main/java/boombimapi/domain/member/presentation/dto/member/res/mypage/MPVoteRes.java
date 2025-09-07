package boombimapi.domain.member.presentation.dto.member.res.mypage;

import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "마이페이지 개별 투표 응답")
public record MPVoteRes(

        @Schema(description = "투표 ID", example = "101")
        Long voteId,

        @Schema(
                description = "사용자 별 프로필 사진 리스트",
                example = "[\"http://k.kakaocdn.net/dn/bN0Hg2/btsIUhLSYs8/vrWzldpNSnycWKkRtYyIgk/img_640x640.jpg\", " +
                        "\"https://example.com/images/profile2.jpg\", " +
                        "\"https://example.com/images/profile3.png\"]"
        )
        List<String> profile,

        @Schema(description = "투표 생성 시간", example = "2025-07-15T09:30:00")
        LocalDateTime day,

        @Schema(description = "장소 이름", example = "강남 교보문고")
        String posName,

        @Schema(description = "가장 많이 투표된 상태 (없음 포함)", example = "CROWED")
        List<VoteAnswerType> popularRes,

        @Schema(description = "여유 투표 수", example = "5")
        Long relaxedCnt,

        @Schema(description = "보통 투표 수", example = "8")
        Long commonly,

        @Schema(description = "약간 붐빔 투표 수", example = "4")
        Long slightlyBusyCnt,

        @Schema(description = "붐빔 투표 수", example = "2")
        Long crowedCnt,

        @Schema(description = "해당 투표 전체 수", example = "18")
        Long voteAllCnt,
        @Schema(description = "해당 상태의 투표 수", example = "18")
        VoteStatus voteStatus


) {
    public static MPVoteRes of(
            Long voteId,
            List<String> profile,
            LocalDateTime day,
            String posName,
            List<VoteAnswerType> popularRes,
            Long relaxedCnt,
            Long commonly,
            Long slightlyBusyCnt,
            Long crowedCnt,
            Long voteAllCnt,
            VoteStatus voteStatus
    ) {
        return new MPVoteRes(
                voteId,
                profile,
                day,
                posName,
                popularRes,
                relaxedCnt,
                commonly,
                slightlyBusyCnt,
                crowedCnt,
                voteAllCnt,
                voteStatus
        );
    }
}
