package boombimapi.global.geo.impl.support;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public final class BucketAccumulator {

    private final long bucketXIndex;
    private final long bucketYIndex;

    private double sumWorldX;
    private double sumWorldY;
    private int count;

    private final List<Long> memberPlaceIds = new ArrayList<>();

    public BucketAccumulator(
        long bucketXIndex,
        long bucketYIndex
    ) {
        this.bucketXIndex = bucketXIndex;
        this.bucketYIndex = bucketYIndex;
    }

    public void add(
        long memberPlaceId,
        double worldX,
        double worldY
    ) {
        this.sumWorldX += worldX;
        this.sumWorldY += worldY;
        this.count++;
        this.memberPlaceIds.add(memberPlaceId);
    }

    public double centerWorldX() {
        if (count == 0) {
            return sumWorldX;
        }
        return sumWorldX / count;
    }

    public double centerWorldY() {
        if (count == 0) {
            return sumWorldY;
        }
        return sumWorldY / count;
    }

}
