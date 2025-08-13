package boombimapi.domain.alarm.presentation.dto.req;

import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;


public record AlarmSendResDto(

        int successCount,
        int failureCount,
        List<String> invalidTokens
){
    public static AlarmSendResDto of(int successCount, int failureCount, List<String> invalidTokens){
        return new AlarmSendResDto(successCount, failureCount, invalidTokens);
    }
}
