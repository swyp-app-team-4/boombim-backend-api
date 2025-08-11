package boombimapi.domain.alarm.application.service.impl;



import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.repository.AlarmRepository;
import boombimapi.domain.alarm.presentation.dto.req.GetAlarmHistoryRequest;
import boombimapi.domain.alarm.presentation.dto.req.RegisterFcmTokenRequest;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.res.AlarmHistoryResponse;

import boombimapi.domain.alarm.presentation.dto.res.RegisterFcmTokenResponse;
import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.user.domain.repository.UserRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    /**
     * 관리자가 알림 전송
     */
    @Async
    @Override
    public CompletableFuture<SendAlarmResponse> sendAlarm(String senderUserId, SendAlarmRequest request) {
        log.info("알림 전송 요청: 발신자={}, 타입={}, 대상={}",
                senderUserId, request.type(), request.targetUserId());

        // 발신자가 관리자인지 확인
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        if (!isAdmin(sender)) {
            throw new BoombimException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }

        // 알림 엔티티 생성
        Alarm alarm = Alarm.builder()
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .senderUserId(senderUserId)
                .targetUserId(request.targetUserId())
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);

        // 알림 전송 처리
        AlarmSendResult sendResult;

        try {
            savedAlarm.updateStatus(AlarmStatus.SENDING);

            if (request.targetUserId() != null) {
                // 특정 사용자에게 전송
                boolean success = fcmService.sendNotificationToUser(
                        request.targetUserId(),
                        request.title(),
                        request.message()
                );
                sendResult = success ?
                        new AlarmSendResult(1, 0, java.util.List.of()) :
                        new AlarmSendResult(0, 1, java.util.List.of());
            } else {
                // 전체 사용자에게 전송
                sendResult = fcmService.sendNotificationToAll(
                        request.title(),
                        request.message()
                ).get(); // 비동기 결과 대기
            }

            // 전송 결과 업데이트
            if (sendResult.failureCount() == 0) {
                savedAlarm.updateStatus(AlarmStatus.SENT);
            } else if (sendResult.successCount() > 0) {
                savedAlarm.updateStatus(AlarmStatus.SENT);
            } else {
                savedAlarm.updateFailureReason("모든 대상자에게 전송 실패");
            }

            log.info("알림 전송 완료: alarmId={}, 성공={}, 실패={}",
                    savedAlarm.getId(), sendResult.successCount(), sendResult.failureCount());

            return CompletableFuture.completedFuture(
                    SendAlarmResponse.of(
                            savedAlarm,
                            sendResult.successCount(),
                            sendResult.failureCount(),
                            sendResult.successCount() + sendResult.failureCount()
                    )
            );

        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: alarmId={}, error={}", savedAlarm.getId(), e.getMessage());
            savedAlarm.updateFailureReason("전송 중 오류: " + e.getMessage());

            return CompletableFuture.completedFuture(
                    SendAlarmResponse.of(savedAlarm, 0, 1, 1)
            );
        }
    }

    /**
     * FCM 토큰 등록
     */
    @Override
    public RegisterFcmTokenResponse registerFcmToken(String userId, RegisterFcmTokenRequest request) {
        try {
            // 사용자 존재 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

            fcmService.registerToken(userId, request.token(), request.deviceType());

            log.info("FCM 토큰 등록 완료: userId={}, deviceType={}", userId, request.deviceType());
            return RegisterFcmTokenResponse.success();

        } catch (BoombimException e) {
            throw e;
        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패: userId={}, error={}", userId, e.getMessage());
            return RegisterFcmTokenResponse.failure("FCM 토큰 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 알림 내역 조회 (관리자용)
     */
    @Override
    public PagedAlarmHistoryResponse getAlarmHistory(String userId, GetAlarmHistoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        if (!isAdmin(user)) {
            throw new BoombimException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }

        Pageable pageable = PageRequest.of(request.page(), request.size());
        Page<Alarm> alarmPage = alarmRepository.findBySenderUserIdOrderByCreatedAtDesc(userId, pageable);

        return new PagedAlarmHistoryResponse(
                alarmPage.getContent().stream()
                        .map(AlarmHistoryResponse::from)
                        .toList(),
                alarmPage.getNumber(),
                alarmPage.getSize(),
                alarmPage.getTotalPages(),
                alarmPage.getTotalElements(),
                alarmPage.isLast()
        );
    }

    /**
     * 특정 알림 상세 조회
     */
    @Override
    public AlarmHistoryResponse getAlarmDetail(String userId, Long alarmId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        if (!isAdmin(user)) {
            throw new BoombimException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }

        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new BoombimException(ErrorCode.OBJECT_NOT_FOUND, "알림을 찾을 수 없습니다."));

        // 본인이 발송한 알림인지 확인
        if (!alarm.getSenderUserId().equals(userId)) {
            throw new BoombimException(ErrorCode.FORBIDDEN, "해당 알림에 대한 권한이 없습니다.");
        }

        return AlarmHistoryResponse.from(alarm);
    }

    /**
     * 관리자 권한 확인
     */
    private boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN");
    }

    /**
     * 사용자의 FCM 토큰 개수 조회
     */
    public int getUserTokenCount(String userId) {
        return fcmService.getUserTokenCount(userId);
    }
}
