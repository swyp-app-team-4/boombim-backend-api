package boombimapi.domain.place.presentation.dto;

public record MapMarker(
    Long id,
    String name,
    Double latitude,
    Double longitude
) {

}
