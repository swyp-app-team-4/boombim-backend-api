package boombimapi.domain.point.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * GetPointRes
 * <p>
 * 회원의 포인트 잔액과 거래 이력을 함께 반환하는 응답 DTO.
 */
@Schema(description = "회원 포인트 잔액 및 거래 이력 응답 DTO")
public record GetPointRes(

        @Schema(description = "현재 포인트 잔액", example = "120")
        Long point,

        @Schema(description = "포인트 거래 이력 리스트")
        List<GetPointHistoryRes> getPointHistoryRes
) {
    /**
     * 포인트 및 거래 이력 DTO 생성 메서드
     *
     * @param point              포인트 잔액
     * @param getPointHistoryRes 포인트 거래 이력 리스트
     * @return GetPointRes 객체
     */
    public static GetPointRes of(Long point, List<GetPointHistoryRes> getPointHistoryRes) {
        return new GetPointRes(point, getPointHistoryRes);
    }
}
