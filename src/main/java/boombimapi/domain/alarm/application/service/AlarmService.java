package boombimapi.domain.alarm.application.service;

import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;

import boombimapi.domain.alarm.presentation.dto.req.RegisterFcmTokenRequest;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;
import boombimapi.domain.alarm.presentation.dto.req.UpdateAlarmStatusReq;
import boombimapi.domain.alarm.presentation.dto.res.HistoryResponse;
import boombimapi.domain.alarm.presentation.dto.res.RegisterFcmTokenResponse;
import boombimapi.domain.alarm.presentation.dto.res.SendAlarmResponse;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.vote.domain.entity.Vote;

import java.util.List;

/**
 * 알림 관리 서비스 인터페이스
 */
public interface AlarmService {

    // 공지 사항 및 이벤트 알림 전송
    SendAlarmResponse sendAllAlarm(String senderUserId, SendAlarmRequest request);

    // FCM 토큰 등록
    RegisterFcmTokenResponse registerFcmToken(String userId, RegisterFcmTokenRequest request);

    // 알림 내역 조회
    List<HistoryResponse> getAlarmHistory(String userId, DeviceType deviceType);

    // 투표 종료 알림
    SendAlarmResponse sendEndVoteAlarm(Vote vote, List<Member> userList, boolean flag);

    // 알림 상태 업데이트 읽었는지 안읽었는지
    void updateAlarmStatus(String userId, UpdateAlarmStatusReq req);


}