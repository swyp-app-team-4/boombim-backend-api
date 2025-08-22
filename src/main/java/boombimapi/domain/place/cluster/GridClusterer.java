package boombimapi.domain.place.cluster;

import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 고정 격자 + 인접(8방향) 병합 클러스터러 - Z_REF 기준 투영 후 부모 접기(shift = Z_REF - zoom) - 인접 버킷은 거리 임계값으로 병합
 */
@Slf4j
public final class GridClusterer {

    public record Point(Long id, double lat, double lng) {

    }

    public record Bucket(double lat, double lng, int count, List<Long> placeIds) {

    }

    private static final double TILE_SIZE = 256.0;

    // ===== 튜닝 포인트 =====
    private static final int Z_REF = 20;            // 앱 최대 줌과 맞춤
    private static final int BASE_CELL_PX = 64;     // Z_REF에서 셀 한 변(px)
    private static final double MERGE_FACTOR = 1.05;// 병합 임계값 보정(셀 변의 ~5% 여유)
    private static final boolean ALLOW_DIAGONAL = true; // 8방향 허용

    // --- Web Mercator ---
    private static double lonToWorldX(double lon, int zoom) {
        double scale = TILE_SIZE * (1 << zoom);
        return (lon + 180.0) / 360.0 * scale;
    }

    private static double latToWorldY(double lat, int zoom) {
        double s = Math.sin(Math.toRadians(lat));
        double ny = 0.5 - Math.log((1 + s) / (1 - s)) / (4 * Math.PI);
        double scale = TILE_SIZE * (1 << zoom);
        return ny * scale;
    }

    private static double worldXToLon(double x, int zoom) {
        double scale = TILE_SIZE * (1 << zoom);
        return (x / scale) * 360.0 - 180.0;
    }

    private static double worldYToLat(double y, int zoom) {
        double scale = TILE_SIZE * (1 << zoom);
        double ny = y / scale;
        double z = Math.PI * (1 - 2 * ny);
        double latRad = Math.atan(Math.sinh(z));
        return Math.toDegrees(latRad);
    }

    /**
     * @param ps                          후보 포인트
     * @param minLat/maxLat/minLng/maxLng 뷰포트 경계(필터용)
     * @param zoom                        현재 줌(클수록 확대)
     */
    public static List<Bucket> cluster(
        List<Point> ps,
        double minLat, double maxLat, double minLng, double maxLng,
        int zoom
    ) {
        if (ps == null || ps.isEmpty())
            return List.of();

        // 줌아웃할수록 부모 셀
        int shift = Math.max(0, Z_REF - zoom);

        log.debug(">>> Zoom: {}, Calculated Shift: {}", zoom, shift);

        // 1) 고정 격자 버킷팅(항상 Z_REF 좌표계)
        Map<Long, Agg> map = new HashMap<>();
        for (Point p : ps) {
            if (p.lat() < minLat || p.lat() > maxLat || p.lng() < minLng || p.lng() > maxLng)
                continue;

            double xRef = lonToWorldX(p.lng(), Z_REF);
            double yRef = latToWorldY(p.lat(), Z_REF);

            long ix0 = (long) Math.floor(xRef / BASE_CELL_PX);
            long iy0 = (long) Math.floor(yRef / BASE_CELL_PX);

            long ix = ix0 >> shift;
            long iy = iy0 >> shift;

            log.trace(">>> Point ID: {} -> Grid Index: ({}, {})", p.id(), ix, iy);

            long key = (ix << 32) ^ (iy & 0xffffffffL);
            map.computeIfAbsent(key, k -> new Agg(ix, iy)).add(p.id(), xRef, yRef);
        }
        if (map.isEmpty())
            return List.of();

        // 2) 인접 버킷 병합
        List<Agg> cells = new ArrayList<>(map.values());
        int n = cells.size();

        Map<Long, Integer> idxByCell = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            Agg a = cells.get(i);
            idxByCell.put((a.ix << 32) ^ (a.iy & 0xffffffffL), i);
        }

        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }

        final double cellPx = (BASE_CELL_PX * (1 << shift));
        final double mergePx = Math.max(cellPx, cellPx * MERGE_FACTOR);
        final double mergePxSq = mergePx * mergePx;

        int unions = 0;

        for (int i = 0; i < n; i++) {
            Agg a = cells.get(i);
            double ax = a.cx(), ay = a.cy();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    if (!ALLOW_DIAGONAL && dx != 0 && dy != 0)
                        continue;

                    long nix = a.ix + dx, niy = a.iy + dy;
                    Integer j = idxByCell.get((nix << 32) ^ (niy & 0xffffffffL));
                    if (j == null)
                        continue;

                    Agg b = cells.get(j);
                    double bx = b.cx(), by = b.cy();
                    double d2 = (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
                    if (d2 <= mergePxSq) {
                        union(parent, i, j);
                        unions++;
                    }
                }
            }
        }

        // 3) 그룹 재집계
        Map<Integer, Agg> byRoot = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int r = find(parent, i);
            Agg cur = cells.get(i);
            Agg root = byRoot.computeIfAbsent(r, k -> new Agg(cur.ix, cur.iy));
            root.merge(cur);
        }

        // 4) 출력
        List<Bucket> out = new ArrayList<>(byRoot.size());
        for (Agg a : byRoot.values()) {
            double cx = a.cx(), cy = a.cy();
            out.add(new Bucket(worldYToLat(cy, Z_REF), worldXToLon(cx, Z_REF), a.c, a.ids));
        }
        return out;
    }

    // Union-Find
    private static int find(int[] p, int x) {
        return p[x] == x ? x : (p[x] = find(p, p[x]));
    }

    private static void union(int[] p, int a, int b) {
        int ra = find(p, a), rb = find(p, b);
        if (ra != rb)
            p[rb] = ra;
    }

    // 누적 구조
    private static final class Agg {

        final long ix, iy;
        double sumX, sumY;
        int c;
        List<Long> ids = new ArrayList<>(4);

        Agg(long ix, long iy) {
            this.ix = ix;
            this.iy = iy;
        }

        void add(Long id, double xRef, double yRef) {
            sumX += xRef;
            sumY += yRef;
            c++;
            ids.add(id);
        }

        void merge(Agg o) {
            sumX += o.sumX;
            sumY += o.sumY;
            c += o.c;
            ids.addAll(o.ids);
        }

        double cx() {
            return sumX / Math.max(1, c);
        }

        double cy() {
            return sumY / Math.max(1, c);
        }
    }
}
