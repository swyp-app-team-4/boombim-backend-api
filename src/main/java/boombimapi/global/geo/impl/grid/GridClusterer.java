//package boombimapi.global.geo.impl.grid;
//
//import boombimapi.domain.place.cluster.Clusterer;
//import boombimapi.domain.place.cluster.vo.ClusterResult;
//import boombimapi.domain.place.cluster.vo.ClusterInput;
//import boombimapi.global.geo.config.ClusterPolicy;
//import boombimapi.global.geo.impl.support.BucketAccumulator;
//import boombimapi.domain.place.cluster.WebMercator;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class GridClusterer implements Clusterer {
//
//    private final ClusterPolicy clusterPolicy;
//
//    @Override
//    public List<ClusterResult> cluster(
//        List<ClusterInput> clusterInputs,
//        int zoomLevel
//    ) {
//        if (clusterInputs == null || clusterInputs.isEmpty()) {
//            return List.of();
//        }
//
//        final int zoomRef = clusterPolicy.zoomRef();
//        final double tileSize = clusterPolicy.tileSize();
//        final int baseCellPixel = clusterPolicy.baseCellPixel();
//        final int shift = Math.max(0, zoomRef - zoomLevel);
//
//        Map<Long, BucketAccumulator> bucketMap = new HashMap<>();
//        for (ClusterInput point : clusterInputs) {
//            double latitude = point.latitude();
//            double longitude = point.longitude();
//
//            double worldXAtRef = WebMercator.longitudeToWorldX(longitude, zoomRef, tileSize);
//            double worldYAtRef = WebMercator.latitudeToWorldY(latitude, zoomRef, tileSize);
//
//            long baseIndexX = (long) Math.floor(worldXAtRef / baseCellPixel);
//            long baseIndexY = (long) Math.floor(worldYAtRef / baseCellPixel);
//
//            long parentIndexX = baseIndexX >> shift;
//            long parentIndexY = baseIndexY >> shift;
//
//            long key = encodeKey(parentIndexX, parentIndexY);
//
//            bucketMap
//                .computeIfAbsent(key, k -> new BucketAccumulator(parentIndexX, parentIndexY))
//                .add(point.id(), worldXAtRef, worldYAtRef);
//        }
//
//        if (bucketMap.isEmpty()) {
//            return List.of();
//        }
//
//        List<ClusterResult> markers = new ArrayList<>(bucketMap.size());
//        for (BucketAccumulator bucket : bucketMap.values()) {
//            double centerWorldX = bucket.centerWorldX();
//            double centerWorldY = bucket.centerWorldY();
//
//            double latitude = WebMercator.worldYToLatitude(centerWorldY, zoomRef, tileSize);
//            double longitude = WebMercator.worldXToLongitude(centerWorldX, zoomRef, tileSize);
//
//            markers.add(new ClusterResult(
//                latitude,
//                longitude,
//                bucket.getCount(),
//                bucket.getMemberPlaceIds() // 불변 리스트 반환
//            ));
//        }
//
//        log.debug("[FixedGridClusterer] points={}, buckets={}, zoom={}, shift={}",
//            clusterInputs.size(), markers.size(), zoomLevel, shift);
//
//        return markers;
//    }
//
//    private static long encodeKey(long bucketXIndex, long bucketYIndex) {
//        return (bucketXIndex << 32) ^ (bucketYIndex & 0xffffffffL);
//    }
//}