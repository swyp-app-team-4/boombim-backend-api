package boombimapi.domain.point.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 이벤트 응모 시 포인트 차감 요청 DTO
 *
 * <p>이 클래스는 사용자가 특정 이벤트에 응모하면서 포인트를 차감할 때 사용됩니다.
 * 이벤트 캠페인 식별자와 차감할 포인트 금액을 전달받습니다.</p>
 */
@Schema(description = "이벤트 응모 포인트 차감 요청")
public record UsePointForEventReq(

        @Schema(description = "이벤트 캠페인 ID", example = "1")
        Long eventCampaignId,

        @Schema(description = "차감할 포인트 금액", example = "1000")
        Long amount

) {}
