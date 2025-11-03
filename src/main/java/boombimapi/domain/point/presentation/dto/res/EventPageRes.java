package boombimapi.domain.point.presentation.dto.res;

import boombimapi.domain.point.domain.entity.EventCampaign;
import java.time.LocalDateTime;

public record EventPageRes(
        Long eventCampaignId,
        LocalDateTime eventStartDate,
        LocalDateTime eventEndDate,
        LocalDateTime winnerAnnouncementDate
) {
    public static EventPageRes of(EventCampaign campaign) {
        return new EventPageRes(
                campaign.getId(),
                campaign.getEventStartDate(),
                campaign.getEventEndDate(),
                campaign.getWinnerAnnouncementDate()
        );
    }
}
