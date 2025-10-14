package boombimapi.domain.point.presentation.dto.res;

import java.util.List;

public record GetPointRes(
        Long point,

        List<GetPointHistoryRes> getPointHistoryRes
) {
    public static GetPointRes of(Long point, List<GetPointHistoryRes> getPointHistoryRes) {
        return new GetPointRes(point, getPointHistoryRes);
    }

}
