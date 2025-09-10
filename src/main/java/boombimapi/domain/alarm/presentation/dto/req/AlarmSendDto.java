package boombimapi.domain.alarm.presentation.dto.req;

import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import lombok.Builder;

import java.util.List;
@Builder
public record AlarmSendDto(

    List<FcmToken> allTokens,
    String title,
    String body,
    Alarm alarm
){}
