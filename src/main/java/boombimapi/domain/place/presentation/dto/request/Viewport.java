package boombimapi.domain.place.presentation.dto.request;

public record Viewport(
    double minLatitude, // 최소 위도
    double minLongitude,    // 최대 위도
    double maxLatitude, // 최소 경도
    double maxLongitude // 최대 경도
) {

}
