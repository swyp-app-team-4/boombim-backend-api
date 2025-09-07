package boombimapi.domain.alarm.application.service;


import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.member.domain.entity.Member;


import java.util.List;


public interface FcmService {


    //  FCM 토큰 등록
    void registerToken(Member user, String token, DeviceType deviceType);

    // 공지용 모든 사용자에게 알림 전송
    AlarmSendResult sendNotificationToAll(String title, String body, Alarm alarm);


    // 투표 종료 알림
    AlarmSendResult sendNotificationToVote(String title, String body, Alarm alarm, List<Member> userList);

    // 오래된 비활성 토큰 정리
    void cleanupOldTokens();

    void deleteFcmToken(String userId);


}
