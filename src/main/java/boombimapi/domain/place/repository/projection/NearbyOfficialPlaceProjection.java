package boombimapi.domain.place.repository.projection;

import java.time.LocalDateTime;

public interface NearbyOfficialPlaceProjection {

    Long getId();

    String getName();

    String getLegalDong();

    String getImageUrl();

    Double getDistanceMeters();

    LocalDateTime getObservedAt();

    String getCongestionLevelName();

}
