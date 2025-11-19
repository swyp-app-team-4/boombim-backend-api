package boombimapi.domain.place.cluster;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CellAccumulator {

    private final long cellX;
    private final long cellY;
    private final List<Long> placeIds = new ArrayList<>();

    private double sumWorldPixelX;
    private double sumWorldPixelY;
    private int count;

    public void add(
        long placeId,
        double worldPixelX,
        double worldPixelY
    ) {
        sumWorldPixelX += worldPixelX;
        sumWorldPixelY += worldPixelY;
        count++;
        placeIds.add(placeId);
    }

    public double centroidWorldPixelX() {
        if (count == 0) {
            return 0;
        }
        return sumWorldPixelX / count;
    }

    public double centroidWorldPixelY() {
        if (count == 0) {
            return 0;
        }
        return sumWorldPixelY / count;
    }

    public void merge(
        CellAccumulator other
    ) {
        this.sumWorldPixelX += other.sumWorldPixelX;
        this.sumWorldPixelY += other.sumWorldPixelY;
        this.count += other.count;
        this.placeIds.addAll(other.placeIds);
    }

}
