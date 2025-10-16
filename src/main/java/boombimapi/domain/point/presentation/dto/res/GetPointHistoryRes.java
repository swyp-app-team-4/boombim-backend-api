package boombimapi.domain.point.presentation.dto.res;

import boombimapi.domain.point.domain.entity.PointHistory;
import boombimapi.domain.point.domain.entity.type.PointAction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * GetPointHistoryRes
 * <p>
 * 포인트 거래 이력 응답 DTO.
 * <br>포인트 적립 또는 차감 내역을 반환한다.
 */
@Schema(description = "포인트 거래 이력 응답 DTO")
public record GetPointHistoryRes(

        @Schema(description = "포인트 거래 이력 ID", example = "101")
        Long pointHistoryId,

        @Schema(description = "거래 후 잔액", example = "180")
        Long balance,

        @Schema(description = "거래 금액 (적립/차감된 포인트)", example = "20")
        Long amount,

        @Schema(description = "거래 발생 일시", example = "2025-10-16T12:45:00")
        LocalDateTime createdAt,

        @Schema(description = "포인트 동작 유형 (EARN: 적립, USE: 사용)", example = "USE")
        PointAction pointAction,

        @Schema(description = "포인트 카테고리 (예: EVENT, CONGESTION 등)", example = "EVENT")
        String pointCategory
) {
    /**
     * PointHistory 엔티티 → GetPointHistoryRes DTO 변환 메서드
     *
     * @param pointHistory 포인트 이력 엔티티
     * @return 변환된 응답 DTO
     */
    public static GetPointHistoryRes of(PointHistory pointHistory) {
        return new GetPointHistoryRes(
                pointHistory.getId(),
                pointHistory.getBalance(),
                pointHistory.getAmount(),
                pointHistory.getCreatedAt(),
                pointHistory.getPointAction(),
                pointHistory.getPointCategory().getKey()
        );
    }
}
