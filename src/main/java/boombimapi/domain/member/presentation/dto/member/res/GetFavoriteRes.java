package boombimapi.domain.member.presentation.dto.member.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "즐겨찾기 응답 DTO")
public record GetFavoriteRes(

        @Schema(description = "장소 프로필 이미지 URL", example = "https://cdn.boombim.com/profile/user123.png")
        String profile,

        @Schema(description = "장소 이름", example = "강남역 스타벅스")
        String posName,

        @Schema(description = "장소 고유 ID", example = "1")
        Long placeId,

        @Schema(description = "혼잡도 응답 타입", example = "CROWDED")
        String answerType,

        @Schema(description = "오늘 올린 인원 수", example = "120")
        String peopleCnt
) {


    public static GetFavoriteRes of(
            String profile,
            String posName,
            Long placeId,
            String answerType,
            String peopleCnt
    ) {
        return new GetFavoriteRes(profile, posName, placeId, answerType, peopleCnt);
    }
}
