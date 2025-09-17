package boombimapi.domain.place.query.dao.row;

public record OfficialPlaceViewportRow(
    Long id,
    String name,
    String legalDong,
    String imageUrl,
    Double centroidLatitude,
    Double centroidLongitude,
    String congestionLevelName,
    String congestionMessage,
    Boolean isFavorite
) {

}
