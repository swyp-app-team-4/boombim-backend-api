package boombimapi.domain.alarm.application.service.impl;


import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.DeliveryStatus;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.domain.repository.AlarmRecipientRepository;
import boombimapi.domain.alarm.domain.repository.AlarmRepository;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.alarm.presentation.dto.req.GetAlarmHistoryRequest;
import boombimapi.domain.alarm.presentation.dto.req.RegisterFcmTokenRequest;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.res.AlarmHistoryResponse;
import boombimapi.domain.alarm.presentation.dto.res.HistoryResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final AlarmRecipientRepository alarmRecipientRepository;

    /**
     * 관리자가 알림 전송
     */
    @Override
    public SendAlarmResponse sendAlarm(String senderUserId, SendAlarmRequest request) {
        log.info("알림 전송 요청: 발신자={}, 타입={}",
                senderUserId, request.type());

        // 발신자가 관리자인지 확인
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        /**
         * 빠른 개발을 위해 일단 뺴겠음
         if (!isAdmin(sender)) {
         throw new BoombimException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
         }*/

        // 알림 엔티티 생성
        Alarm alarm = Alarm.builder()
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .sender(sender)
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);

        // 알림 전송 처리
        AlarmSendResult sendResult;

        try {
            savedAlarm.updateStatus(AlarmStatus.SENDING);

            /*** 단일 사용자인데 나중에 쓸 곳 있을거 같아서 빼겠음
             *             if (request.targetUserId() != null) {
             *                 // 특정 사용자에게 전송
             *                 boolean success = fcmService.sendNotificationToUser(
             *                         request.targetUserId(),
             *                         request.title(),
             *                         request.message()
             *                 );
             *                 sendResult = success ?
             *                         new AlarmSendResult(1, 0, java.util.List.of()) :
             *                         new AlarmSendResult(0, 1, java.util.List.of());
             *             }
             */


            // 전체 사용자에게 전송
            sendResult = fcmService.sendNotificationToAll(
                    request.title(),
                    request.message(),
                    alarm
            ); // 비동기 결과 대기


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

            return
                    SendAlarmResponse.of(
                            savedAlarm,
                            sendResult.successCount(),
                            sendResult.failureCount(),
                            sendResult.successCount() + sendResult.failureCount()
                    );

        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: alarmId={}, error={}", savedAlarm.getId(), e.getMessage());
            savedAlarm.updateFailureReason("전송 중 오류: " + e.getMessage());
            //throw new BoombimException(ErrorCode.FCM_SEND_FAILED);
            return
                    SendAlarmResponse.of(savedAlarm, 0, 1, 1);
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

            fcmService.registerToken(user, request.token(), request.deviceType());

            log.info("FCM 토큰 등록 완료: userId={}, deviceType={}", userId, request.deviceType());
            return RegisterFcmTokenResponse.sucess();

        } catch (BoombimException e) {
            throw new BoombimException(ErrorCode.FCM_TOKEN_REGISTER_FAILED);
        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패: userId={}, error={}", userId, e.getMessage());
            return RegisterFcmTokenResponse.failure("FCM 토큰 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 알림 내역 조회
     */
    @Override
    public List<HistoryResponse> getAlarmHistory(String userId, GetAlarmHistoryRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        /*** // 나중에 관리자 권함 추가
         *         if (!isAdmin(user)) {
         *             throw new BoombimException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
         *         }
         */


        List<AlarmRecipient> alarmHistores = alarmRecipientRepository.findAllByUserAndDeviceTypeOrderByCreatedAtAsc(user, DeviceType.valueOf(req.deviceType()));

        List<HistoryResponse> result = new ArrayList<>();

        for (AlarmRecipient alarmHistory : alarmHistores) {
            if (!alarmHistory.getDeliveryStatus().equals(DeliveryStatus.FAILED)) {
                result.add(new HistoryResponse(alarmHistory.getAlarm().getTitle(), alarmHistory.getAlarm().getMessage(), alarmHistory.getAlarm().getType(), alarmHistory.getDeliveryStatus()));
            }
        }

        return result;
    }


    private boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN");
    }


}
