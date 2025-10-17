package boombimapi.domain.place.cluster.impl;

import boombimapi.domain.place.cluster.CellAccumulator;
import boombimapi.domain.place.cluster.Clusterer;
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
        final int shift = Math.max(0, zoomLevel - maxZoomAtRefZ);
        final int cellSizePixel = baseCellPixel << shift;

        log.info(">>> GridClusterer cellSizePixel: {}", cellSizePixel);

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

        List<ClusterResult> clusterResults = new ArrayList<>(cellAccumulators.size());

        for (Map.Entry<Cell, CellAccumulator> entry : cellAccumulators.entrySet()) {
            Cell cell = entry.getKey();
            CellAccumulator cellAccumulator = entry.getValue();

            clusterResults.add(ClusterResult.of(
                cell,
                cellAccumulator.centroidWorldPixelX(),
                cellAccumulator.centroidWorldPixelY(),
                cellAccumulator.getCount(),
                cellAccumulator.getPlaceIds()
            ));
        }

        return clusterResults;
    }
}
