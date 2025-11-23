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



        return mergeNeighbors(
            cellAccumulators,
            cellSizePixel
        );
    }

    private List<ClusterResult> mergeNeighbors(
        Map<Cell, CellAccumulator> cellAccumulators,
        int cellSizePixel
    ) {
        if (cellAccumulators.isEmpty()) {
            return List.of();
        }

        int n = cellAccumulators.size();

        List<Cell> cells = new ArrayList<>(cellAccumulators.keySet());
        Map<Cell, Integer> indexByCell = new HashMap<>(n);

        for (int i = 0; i < n; i++) {
            indexByCell.put(cells.get(i), i);
        }

        DisjointSet disjointSet = new DisjointSet(n);

        double mergeDistance = cellSizePixel * 0.95;

        for (int i = 0; i < n; i++) {
            Cell cell = cells.get(i);
            CellAccumulator cellAccumulator = cellAccumulators.get(cell);

            double cellX = cellAccumulator.centroidWorldPixelX();
            double cellY = cellAccumulator.centroidWorldPixelY();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    Cell neighborCell = new Cell(cell.x() + dx, cell.y() + dy);
                    CellAccumulator neighborAccumulator = cellAccumulators.get(neighborCell);

                    if (neighborAccumulator == null) {
                        continue;
                    }

                    Integer j = indexByCell.get(neighborCell);
                    if (j == null) {
                        continue;
                    }

                    double neighborX = neighborAccumulator.centroidWorldPixelX();
                    double neighborY = neighborAccumulator.centroidWorldPixelY();

                    double distance = Math.hypot(cellX - neighborX, cellY - neighborY);
                    if (distance <= mergeDistance) {
                        disjointSet.union(i, j);
                    }
                }
            }
        }

        Map<Integer, CellAccumulator> mergedAccumulators = new HashMap<>();

        for (int i = 0; i < n; i++) {
            int root = disjointSet.findRoot(i);

            Cell cell = cells.get(i);
            CellAccumulator original = cellAccumulators.get(cell);

            CellAccumulator merged = mergedAccumulators.computeIfAbsent(
                root,
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
