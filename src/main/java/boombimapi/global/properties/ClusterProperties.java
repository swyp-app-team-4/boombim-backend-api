package boombimapi.global.properties;

public sealed interface ClusterProperties
    permits WebClusterProperties, AppClusterProperties {

    int refZ();

    int maxZoomAtRefZ();

    int baseCellPixel();

    double tileSize();

    int minClusterSize();
}
