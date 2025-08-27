package boombimapi.global.geo.internal;

public final class GeoDistance {

    public static final double EARTH_RADIUS_METERS = 6_371_000d;

    private GeoDistance() {
    }

    public static double haversineMeters(
        double latitudeA,
        double longitudeA,
        double latitudeB,
        double longitudeB
    ) {
        double latitudeDeltaRadians = Math.toRadians(latitudeB - latitudeA);
        double longitudeDeltaRadians = Math.toRadians(longitudeB - longitudeA);

        double a = Math.pow(Math.sin(latitudeDeltaRadians / 2.0), 2.0)
            + Math.cos(Math.toRadians(latitudeA)) * Math.cos(Math.toRadians(latitudeB))
            * Math.pow(Math.sin(longitudeDeltaRadians / 2.0), 2.0);

        double centralAngle = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_METERS * centralAngle;
    }
}