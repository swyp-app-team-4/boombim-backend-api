package boombimapi.domain.place.repository.projection;

public interface NearbyOfficialPlaceProjection {

    Long getId();

    String getName();

    String getImageUrl();

    Double getDistanceMeters();

    String getCongestionLevelName();

}
