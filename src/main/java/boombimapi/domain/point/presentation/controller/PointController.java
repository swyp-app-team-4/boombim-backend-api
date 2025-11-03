package boombimapi.domain.point.presentation.controller;

import boombimapi.domain.point.application.PointService;
import boombimapi.domain.point.presentation.dto.req.UsePointForEventReq;
import boombimapi.domain.point.presentation.dto.res.EventPageRes;
import boombimapi.domain.point.presentation.dto.res.GetPointRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * PointController 포인트 관련 API 엔드포인트를 제공한다.
 * <p>
 * - 포인트 내역 조회<br> - 이벤트 응모(포인트 차감)
 */
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Point", description = "포인트 관련 API")
public class PointController {

    private final PointService pointService;

    /**
     * [GET] 포인트 내역 조회 API
     * <p>
     * - 회원의 포인트 잔액 및 거래 이력을 조회한다.<br> - 인증된 사용자(@AuthenticationPrincipal)의 memberId를 기반으로 조회 수행.
     *
     * @param memberId 인증된 회원 ID
     * @return 포인트 잔액 및 거래 이력 응답 DTO
     */
    @Operation(summary = "포인트 내역 조회 API", description = "회원의 포인트 잔액과 거래 이력을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포인트 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "포인트 정보가 존재하지 않음")
    })
    @GetMapping("/point")
    public ResponseEntity<GetPointRes> getPointHistory(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(pointService.getPointHistory(memberId));
    }

    /**
     * [PATCH] 이벤트 응모 API
     * <p>
     * - 회원이 이벤트에 응모하며, 포인트 20점을 차감한다.<br> - 포인트 잔액 부족 또는 응모 횟수 초과 시 예외 발생.
     *
     * @param memberId 인증된 회원 ID
     * @param req      이벤트 응모 요청 (이벤트 캠페인 ID 및 차감할 포인트 금액 포함)
     * @return HTTP 200 OK (성공 시 바디 없음)
     */
    @Operation(summary = "이벤트 응모 API", description = "이벤트에 응모하여 포인트를 차감합니다. (1회당 20포인트 차감)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포인트 차감 및 이벤트 응모 성공"),
            @ApiResponse(responseCode = "400", description = "응모 횟수 초과 또는 포인트 부족"),
            @ApiResponse(responseCode = "404", description = "포인트 정보가 존재하지 않음")
    })
    @PatchMapping("/point")
    public ResponseEntity<Void> applyEvent(@AuthenticationPrincipal String memberId,
                                           @RequestBody UsePointForEventReq req) {
        pointService.usePointForEvent(memberId, req);
        return ResponseEntity.ok().build();
    }

    /**
     * [GET] 진행 중인 이벤트 페이지 조회 API
     * <p>
     * - 현재 진행 중인 이벤트 캠페인을 조회한다.<br>
     * - 가장 최근 생성된 이벤트 캠페인 기준으로 데이터를 반환한다.<br>
     * - 이벤트 기간(시작일, 종료일, 당첨자 발표일) 정보를 포함한다.
     *
     * @return 진행 중인 이벤트 페이지 응답 DTO
     */
    @Operation(summary = "진행 중인 이벤트 페이지 조회 API", description = "현재 진행 중인 이벤트 캠페인을 조회합니다. (이벤트 기간 및 당첨자 발표일 포함)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 페이지 조회 성공"),
            @ApiResponse(responseCode = "404", description = "진행 중인 이벤트가 존재하지 않음")
    })
    @GetMapping("/event")
    public ResponseEntity<EventPageRes> getOngoingEventPage() {
        return ResponseEntity.ok(pointService.getOngoingEventPage());
    }

}
