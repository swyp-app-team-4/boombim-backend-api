package boombimapi.global.geo.internal;

/**
 * Web Mercator
 * 위도·경도 ↔︎ 월드 픽셀 좌표(줌/타일 크기 기반) 변환 제공
 */
public final class WebMercator {

    private WebMercator() {

    }

    /**
     * 경도를 월드 X 픽셀 좌표로 변환
     *
     * @param longitude 경도
     * @param zoomLevel 줌 레벨
     * @param tileSize  타일 한 변의 픽셀 수
     * @return 월드 X 픽셀 좌표 [0, tileSize * 2^zoomLevel]
     */
    public static double longitudeToWorldX(
        double longitude,
        int zoomLevel,
        double tileSize
    ) {
        double scale = tileSize * (1 << zoomLevel);
        return (longitude + 180.0) / 360.0 * scale;
    }

    /**
     * 위도를 월드 Y 픽셀 좌표로 변환
     *
     * @param latitude  위도
     * @param zoomLevel 줌 레벨
     * @param tileSize  타일 한 변의 픽셀 수
     * @return 월드 Y 픽셀 좌표 [0, tileSize * 2^zoomLevel]
     */
    public static double latitudeToWorldY(
        double latitude,
        int zoomLevel,
        double tileSize
    ) {
        double sineOfLatitude = Math.sin(Math.toRadians(latitude));
        double normalizedY = 0.5 - Math.log((1 + sineOfLatitude) / (1 - sineOfLatitude)) / (4 * Math.PI);
        double scale = tileSize * (1 << zoomLevel);
        return normalizedY * scale;
    }

    /**
     * 월드 X 픽셀 좌표를 경도로 변환
     *
     * @param worldX    월드 X 픽셀 좌표
     * @param zoomLevel 줌 레벨
     * @param tileSize  타일 한 변의 픽셀 수
     * @return 경도
     */
    public static double worldXToLongitude(
        double worldX,
        int zoomLevel,
        double tileSize
    ) {
        double scale = tileSize * (1 << zoomLevel);
        return (worldX / scale) * 360.0 - 180.0;
    }

    /**
     * 월드 Y 픽셀 좌표를 위도로 변환
     *
     * @param worldY    월드 Y 픽셀 좌표
     * @param zoomLevel 줌 레벨
     * @param tileSize  타일 한 변의 픽셀 수
     * @return 위도
     */
    public static double worldYToLatitude(
        double worldY,
        int zoomLevel,
        double tileSize
    ) {
        double scale = tileSize * (1 << zoomLevel);
        double normalizedY = worldY / scale;
        double angle = Math.PI * (1 - 2 * normalizedY);
        double latitudeRadians = Math.atan(Math.sinh(angle));
        return Math.toDegrees(latitudeRadians);
    }

}
