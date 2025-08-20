package boombimapi.domain.alarm.application.service.impl;


import boombimapi.domain.alarm.application.messaging.PushNowMessage;
import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.alarm.domain.entity.alarm.type.DeliveryStatus;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.domain.repository.AlarmRecipientRepository;
import boombimapi.domain.alarm.domain.repository.AlarmRepository;
import boombimapi.domain.alarm.infra.messaging.PushProducer;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.alarm.presentation.dto.req.RegisterFcmTokenRequest;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.req.UpdateAlarmStatusReq;
import boombimapi.domain.alarm.presentation.dto.res.HistoryResponse;
import boombimapi.domain.alarm.presentation.dto.res.RegisterFcmTokenResponse;
import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.scheduled.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final MemberRepository userRepository;
    private final FcmService fcmService;
    private final AlarmRecipientRepository alarmRecipientRepository;
    private final PushProducer pushProducer;
    private final MessageService messageService;

    @Value("${admin.id}")
    private String adminId;

    /**
     * 관리자가 알림 전송
     */
    //@Override
    public SendAlarmResponse sendAllAlarmV0(String senderUserId, SendAlarmRequest request) {
        Member sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        // 관리자 권한 체크는 필요 시 활성화
        // if (!isAdmin(sender)) throw new BoombimException(ErrorCode.ADMIN_PERMISSION_REQUIRED);

        // 1) 알림 엔티티 생성 (상태: QUEUED)
        Alarm alarm = Alarm.builder()
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .sender(sender)
                .build();
        alarm.updateStatus(AlarmStatus.QUEUED);
        Alarm saved = alarmRepository.save(alarm);

        // 2) MQ 발행 (즉시 반환)
        PushNowMessage msg = PushNowMessage.builder()
                .alarmId(saved.getId())
                .title(request.title())
                .body(request.message())
                .retryCount(0)
                .build();
        pushProducer.publishNow(msg);
        log.info("푸시요청 발행 완료: alarmId={}", saved.getId());

        // 3) 응답 (전송은 비동기 처리)
        return SendAlarmResponse.of(saved, 0, 0, 0);
    }

    /**
     * FCM 토큰 등록
     */
    @Override
    public RegisterFcmTokenResponse registerFcmToken(String userId, RegisterFcmTokenRequest request) {
        try {
            // 사용자 존재 확인
            Member user = userRepository.findById(userId)
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
    public List<HistoryResponse> getAlarmHistory(String userId, DeviceType deviceType) {
        Member user = userRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        /*** // 나중에 관리자 권함 추가
         *         if (!isAdmin(user)) {
         *             throw new BoombimException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
         *         }
         */


        List<AlarmRecipient> alarmHistores = alarmRecipientRepository.findAllByMemberAndDeviceTypeOrderByCreatedAtAsc(user, DeviceType.valueOf(deviceType.name()));

        List<HistoryResponse> result = new ArrayList<>();

        for (AlarmRecipient alarmHistory : alarmHistores) {
            if (!alarmHistory.getDeliveryStatus().equals(DeliveryStatus.FAILED)) {
                result.add(new HistoryResponse(alarmHistory.getId(), alarmHistory.getAlarm().getTitle(), alarmHistory.getAlarm().getType(), alarmHistory.getDeliveryStatus(), alarmHistory.getAlarm().getCreatedAt()));
            }
        }

        return result;
    }

    @Override
    public SendAlarmResponse sendEndVoteAlarm(Vote vote, List<Member> userList, boolean flag) {

        Member sender = userRepository.findById(adminId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));
        String title = messageService.endAlarmTitle(vote);
        String message;
        // 질문자들
        if(flag){
            message = messageService.endVoteQuestionAlarmMessage(vote);
        }
        // 투표자들
        else{
            message = messageService.endVoteAnswerAlarmMessage(vote);
        }



        // 알림 엔티티 생성
        Alarm alarm = Alarm.builder()
                .title(title)
                .message(message)
                .type(AlarmType.VOTE)
                .sender(sender)
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);

        // 알림 전송 처리
        AlarmSendResult sendResult;

        try {
            savedAlarm.updateStatus(AlarmStatus.SENDING);

            // 투표한 사람들에게 전송
            sendResult = fcmService.sendNotificationToVote(
                    title,
                    message,
                    alarm,
                    userList
            );


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

            return SendAlarmResponse.of(
                    savedAlarm,
                    sendResult.successCount(),
                    sendResult.failureCount(),
                    sendResult.successCount() + sendResult.failureCount()
            );

        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: alarmId={}, error={}", savedAlarm.getId(), e.getMessage());
            savedAlarm.updateFailureReason("전송 중 오류: " + e.getMessage());
            //throw new BoombimException(ErrorCode.FCM_SEND_FAILED);
            return SendAlarmResponse.of(savedAlarm, 0, 1, 1);
        }
    }

    @Override
    public void updateAlarmStatus(String userId, UpdateAlarmStatusReq req) {
        Member member = userRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        AlarmRecipient ar = alarmRecipientRepository.findById(req.alarmReId()).orElse(null);
        if (ar == null) throw new BoombimException(ErrorCode.ALARM_NOT_FOUND);

        if (!Objects.equals(ar.getMember().getId(), member.getId()))
            throw new BoombimException(ErrorCode.ALARM_ACCESS_DENIED);

        ar.updateDeliveryStatus();
    }


    private boolean isAdmin(Member user) {
        return user.getRole().name().equals("ADMIN");
    }



    public SendAlarmResponse sendAllAlarm(String senderUserId, SendAlarmRequest request) {
        Member sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        //boolean admin = isAdmin(sender);
        //if (!admin) throw new BoombimException(ErrorCode.ADMIN_PERMISSION_REQUIRED);

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

            // 전체 사용자에게 전송
            sendResult = fcmService.sendNotificationToAll(
                    request.title(),
                    request.message(),
                    alarm
            );


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

            return SendAlarmResponse.of(
                    savedAlarm,
                    sendResult.successCount(),
                    sendResult.failureCount(),
                    sendResult.successCount() + sendResult.failureCount()
            );

        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: alarmId={}, error={}", savedAlarm.getId(), e.getMessage());
            savedAlarm.updateFailureReason("전송 중 오류: " + e.getMessage());
            //throw new BoombimException(ErrorCode.FCM_SEND_FAILED);
            return SendAlarmResponse.of(savedAlarm, 0, 1, 1);
        }
    }


}
