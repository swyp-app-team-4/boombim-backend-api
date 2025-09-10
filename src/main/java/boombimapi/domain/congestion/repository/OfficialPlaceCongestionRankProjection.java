package boombimapi.domain.congestion.repository;

import java.time.LocalDateTime;

public interface OfficialPlaceCongestionRankProjection {

    Long getOfficialPlaceId();

    String getOfficialPlaceName();

    String getLegalDong();

    String getImageUrl();

    String getCongestionLevelName();

    Double getDensityPerM2();

    LocalDateTime getObservedAt();

}
