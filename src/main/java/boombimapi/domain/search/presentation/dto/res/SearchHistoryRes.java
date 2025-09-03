package boombimapi.domain.search.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검색 내역 조회")
public record SearchHistoryRes(
        @Schema(description = "검색 ID")
        Long searchId,
        @Schema(description = "검색 장소 이름")
        String posName
) {
    public static SearchHistoryRes of(Long searchId, String posName){
        return new SearchHistoryRes(searchId, posName);
    }
}
