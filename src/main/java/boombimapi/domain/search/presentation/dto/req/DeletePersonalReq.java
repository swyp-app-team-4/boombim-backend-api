package boombimapi.domain.search.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검색 개별 삭제 DTO")
public record DeletePersonalReq(

        @Schema(description = "검색 ID")
        Long searchId
) {
}
