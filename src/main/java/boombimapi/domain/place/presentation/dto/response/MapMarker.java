package boombimapi.domain.place.presentation.dto.response;

public record MapMarker(
    Long id,
    String name,
    Double latitude,
    Double longitude
) {

}
