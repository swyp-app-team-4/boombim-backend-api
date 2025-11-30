package boombimapi.domain.place.cluster.impl;

import boombimapi.domain.place.cluster.CellAccumulator;
import boombimapi.domain.place.cluster.Clusterer;
import boombimapi.domain.place.cluster.DisjointSet;
import boombimapi.domain.place.cluster.WebMercator;
import boombimapi.domain.place.cluster.vo.Cell;
import boombimapi.domain.place.cluster.vo.ClusterInput;
import boombimapi.domain.place.cluster.vo.ClusterResult;
import boombimapi.global.properties.ClusterProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GridClusterer implements Clusterer {

    private static final double MERGE_DISTANCE_RATIO = 0.98;

    private final ClusterProperties properties;

    @Override
    public List<ClusterResult> cluster(
        List<ClusterInput> clusterInputs,
        int zoomLevel
    ) {

        if (clusterInputs == null || clusterInputs.isEmpty()) {
            return List.of();
        }

        final int refZ = properties.refZ();
        final int baseCellPixel = properties.baseCellPixel();
        final double tileSize = properties.tileSize();
        final int maxZoomAtRefZ = properties.maxZoomAtRefZ();
        int shift;

        if (properties.invertedZoom()) {
            shift = Math.max(0, maxZoomAtRefZ - zoomLevel);
        } else {
            shift = Math.max(0, zoomLevel - maxZoomAtRefZ);
        }

        final int cellSizePixel = baseCellPixel << shift;

        Map<Cell, CellAccumulator> cellAccumulators = new HashMap<>();

        for (ClusterInput clusterInput : clusterInputs) {
            double worldPixelX = WebMercator.longitudeToWorldPixelX(
                clusterInput.longitude(),
                refZ,
                tileSize
            );

            double worldPixelY = WebMercator.latitudeToWorldPixelY(
                clusterInput.latitude(),
                refZ,
                tileSize
            );

            long cellX = (long) Math.floor(worldPixelX / cellSizePixel);
            long cellY = (long) Math.floor(worldPixelY / cellSizePixel);

            Cell cell = new Cell(cellX, cellY);

            cellAccumulators
                .computeIfAbsent(cell, k -> new CellAccumulator(cellX, cellY))
                .add(clusterInput.id(), worldPixelX, worldPixelY);
        }

        // 기존: 인접 셀 병합 (Union-Find)
        // return mergeNeighbors(
        //     cellAccumulators,
        //     cellSizePixel
        // );

        // 임시: mergeNeighbors() 적용 없이, 셀 단위 그대로 클러스터 결과 생성
        List<ClusterResult> clusterResults = new ArrayList<>(cellAccumulators.size());

        for (CellAccumulator accumulator : cellAccumulators.values()) {
            Cell cell = new Cell(accumulator.getCellX(), accumulator.getCellY());

            clusterResults.add(ClusterResult.of(
                cell,
                accumulator.centroidWorldPixelX(),
                accumulator.centroidWorldPixelY(),
                accumulator.getCount(),
                accumulator.getPlaceIds()
            ));
        }

        return clusterResults;
    }

    private List<ClusterResult> mergeNeighbors(
        Map<Cell, CellAccumulator> cellAccumulators,
        int cellSizePixel
    ) {
        if (cellAccumulators.isEmpty()) {
            return List.of();
        }

        int cellCount = cellAccumulators.size();

        List<Cell> cells = new ArrayList<>(cellAccumulators.keySet());
        Map<Cell, Integer> indexByCell = new HashMap<>(cellCount);

        for (int i = 0; i < cellCount; i++) {
            indexByCell.put(cells.get(i), i);
        }

        DisjointSet disjointSet = new DisjointSet(cellCount);

        double mergeDistance = cellSizePixel * MERGE_DISTANCE_RATIO;

        for (int currentCellIndex = 0; currentCellIndex < cellCount; currentCellIndex++) {
            Cell currentCell = cells.get(currentCellIndex);
            CellAccumulator cellAccumulator = cellAccumulators.get(currentCell);

            double currentCellX = cellAccumulator.centroidWorldPixelX();
            double currentCellY = cellAccumulator.centroidWorldPixelY();

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0) {
                        continue;
                    }

                    Cell neighborCell = new Cell(currentCell.x() + offsetX, currentCell.y() + offsetY);
                    CellAccumulator neighborCellAccumulator = cellAccumulators.get(neighborCell);

                    if (neighborCellAccumulator == null) {
                        continue;
                    }

                    Integer neighborCellIndex = indexByCell.get(neighborCell);
                    if (neighborCellIndex == null) {
                        continue;
                    }

                    double neighborCellX = neighborCellAccumulator.centroidWorldPixelX();
                    double neighborCellY = neighborCellAccumulator.centroidWorldPixelY();

                    double distance = Math.hypot(
                        currentCellX - neighborCellX,
                        currentCellY - neighborCellY
                    );

                    if (distance <= mergeDistance) {
                        disjointSet.union(currentCellIndex, neighborCellIndex);
                    }
                }
            }
        }

        Map<Integer, CellAccumulator> mergedAccumulators = new HashMap<>();

        for (int i = 0; i < cellCount; i++) {
            int rootIndex = disjointSet.findRoot(i);

            Cell cell = cells.get(i);
            CellAccumulator original = cellAccumulators.get(cell);

            CellAccumulator merged = mergedAccumulators.computeIfAbsent(
                rootIndex,
                r -> new CellAccumulator(cell.x(), cell.y())
            );

            merged.merge(original);
        }

        List<ClusterResult> clusterResults = new ArrayList<>(mergedAccumulators.size());

        for (CellAccumulator accumulator : mergedAccumulators.values()) {
            Cell cell = new Cell(accumulator.getCellX(), accumulator.getCellY());

            clusterResults.add(ClusterResult.of(
                cell,
                accumulator.centroidWorldPixelX(),
                accumulator.centroidWorldPixelY(),
                accumulator.getCount(),
                accumulator.getPlaceIds()
            ));
        }

        return clusterResults;
    }
}
