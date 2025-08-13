package boombimapi.domain.alarm.application.service;


import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import boombimapi.domain.user.domain.entity.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface FcmService {


    //  FCM 토큰 등록
    void registerToken(User user, String token, DeviceType deviceType);

    // 공지용 모든 사용자에게 알림 전송
    AlarmSendResult sendNotificationToAll(String title, String body, Alarm alarm);


    // 투표 종료 알림
    AlarmSendResult sendNotificationToVote(String title, String body, Alarm alarm, List<User> userList);

    // 오래된 비활성 토큰 정리
    void cleanupOldTokens();


}
