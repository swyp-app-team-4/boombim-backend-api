package boombimapi.domain.point.presentation.dto.req;

import boombimapi.domain.point.domain.entity.type.EventCategory;
import java.time.LocalDateTime;

/**
 * 이벤트 캠페인 등록 요청 DTO
 * <p>
 * - 이벤트 시작일, 종료일, 당첨자 발표일, 카테고리 정보를 포함한다.
 */
public record CreateEventCampaignReq(
        LocalDateTime eventStartDate,
        LocalDateTime eventEndDate,
        LocalDateTime winnerAnnouncementDate,
        EventCategory eventCategory
) {
}
