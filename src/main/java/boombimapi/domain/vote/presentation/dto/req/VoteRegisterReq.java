package boombimapi.domain.vote.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 생성 요청 DTO")
public record VoteRegisterReq(

        @Schema(description = "장소 ID", example = "POS12345")
        String posId,

        @Schema(description = "장소 위도", example = "37.5665")
        double posLatitude,

        @Schema(description = "장소 경도", example = "126.9780")
        double posLongitude,

        @Schema(description = "사용자 현재 위도", example = "37.5651")
        double userLatitude,

        @Schema(description = "사용자 현재 경도", example = "126.9779")
        double userLongitude,

        @Schema(description = "장소 이름", example = "서울역")
        String posName
) {
}
