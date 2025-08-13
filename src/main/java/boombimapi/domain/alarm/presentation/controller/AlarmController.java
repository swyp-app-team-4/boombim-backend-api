package boombimapi.domain.alarm.presentation.controller;


import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.presentation.dto.req.RegisterFcmTokenRequest;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.res.HistoryResponse;
import boombimapi.domain.alarm.presentation.dto.res.RegisterFcmTokenResponse;
import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alarm", description = "알림 관련 API")
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/fcm-token")
    public ResponseEntity<RegisterFcmTokenResponse> registerFcmToken(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody RegisterFcmTokenRequest request) {

        log.info("FCM 토큰 등록 요청: userId={}, deviceType={}", userId, request.deviceType());

        return ResponseEntity.ok(alarmService.registerFcmToken(userId, request));
    }

    @Operation(summary = "알림 전송 (이벤트/공지)", description = "관리자가 사용자들에게 알림을 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 전송 시작"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요 // 일단 안넣음")
    })
    @PostMapping("/send")
    public ResponseEntity<SendAlarmResponse> sendAlarm(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SendAlarmRequest request) {

        log.info("알림 전송 요청: 관리자={}, 제목={}", userId, request.title());

        return ResponseEntity.ok(alarmService.sendAlarm(userId, request));

    }

    @Operation(summary = "사용자별 알림 내역 조회", description = "관리자가 발송한 알림 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
    })
    @GetMapping("/history")
    public ResponseEntity<List<HistoryResponse>> getAlarmHistory(
            @AuthenticationPrincipal String userId,
            @RequestParam DeviceType deviceType
            ) {
        return ResponseEntity.ok(alarmService.getAlarmHistory(userId, deviceType));
    }


}
