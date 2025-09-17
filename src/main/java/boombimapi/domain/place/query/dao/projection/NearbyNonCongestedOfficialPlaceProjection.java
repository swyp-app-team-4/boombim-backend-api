package boombimapi.domain.place.query.dao.projection;

import java.time.LocalDateTime;

public interface NearbyNonCongestedOfficialPlaceProjection {

    Long getId();

    String getName();

    String getLegalDong();

    String getImageUrl();

    Double getDistanceMeters();

    LocalDateTime getObservedAt();

    String getCongestionLevelName();

}
