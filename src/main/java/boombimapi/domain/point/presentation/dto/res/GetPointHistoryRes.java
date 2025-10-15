package boombimapi.domain.point.presentation.dto.res;

import boombimapi.domain.point.domain.entity.Point;
import boombimapi.domain.point.domain.entity.PointHistory;
import boombimapi.domain.point.domain.entity.type.PointAction;
import boombimapi.domain.point.domain.entity.type.PointCategory;

import java.time.LocalDateTime;

public record GetPointHistoryRes(
        Long pointHistoryId,

        Long balance,

        Long amount,

        LocalDateTime createdAt,

        PointAction pointAction,

        String pointCategory
) {
    public static GetPointHistoryRes of(PointHistory pointHistory){
        return new GetPointHistoryRes(
                pointHistory.getId(),
                pointHistory.getBalance(),
                pointHistory.getAmount(),
                pointHistory.getCreatedAt(),
                pointHistory.getPointAction(),
                pointHistory.getPointCategory().getKey());
    }
}
