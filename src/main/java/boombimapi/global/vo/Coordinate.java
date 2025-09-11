package boombimapi.global.vo;

public record Coordinate(
    double latitude,
    double longitude
) {

    public static Coordinate of(
        double latitude,
        double longitude
    ) {
        return new Coordinate(latitude, longitude);
    }

}
