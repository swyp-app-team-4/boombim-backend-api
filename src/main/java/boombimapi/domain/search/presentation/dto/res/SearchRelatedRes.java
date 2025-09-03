package boombimapi.domain.search.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연관 검색어")
public record SearchRelatedRes(

        @Schema(description = "검색 장소 이름")
        String posName
) {
    public static SearchRelatedRes of(String posName){
        return new SearchRelatedRes(posName);
    }
}
