package boombimapi.domain.point.presentation.controller;

import boombimapi.domain.point.application.PointService;
import boombimapi.domain.point.presentation.dto.res.GetPointHistoryRes;
import boombimapi.domain.point.presentation.dto.res.GetPointRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Point", description = "point 전용 API")
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 내역 조회 API", description = "포인트 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
    })
    @GetMapping
    public ResponseEntity<GetPointRes> getPointHistory(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(pointService.getPointHistory(memberId));
    }


    @Operation(summary = "이벤트 응모 API", description = "이벤트 응모를 해서 포인트를 차감합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "포인트가 존재하지 않음"),
    })
    @PatchMapping
    public ResponseEntity<Void> postPointEvnet(@AuthenticationPrincipal String memberId) {
        pointService.usePointForEvent(memberId, 20L);
        return ResponseEntity.ok().build();
    }
}
