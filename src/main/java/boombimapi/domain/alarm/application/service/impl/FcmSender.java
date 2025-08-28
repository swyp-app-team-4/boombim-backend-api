package boombimapi.domain.alarm.application.service.impl;

import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.alarm.presentation.dto.AlarmSendResult;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// package: boombimapi.domain.alarm.application.service.impl
@Service
@RequiredArgsConstructor
public class FcmSender { // 새 컴포넌트명

    private final FirebaseMessaging firebaseMessaging;

    // === 기존 sendBatchNotification 코드 거의 그대로 ===
    public AlarmSendResult sendBatchNotification(List<FcmToken> tokens, String title, String body) {
        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getToken)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                // 중복 토큰도 전부 보내려면 distinct() 쓰지 마세요
                .toList();

        if (tokenStrings.isEmpty()) return new AlarmSendResult(0, 0, List.of());

        int successCount = 0;
        int failureCount = 0;
        List<String> invalidTokens = new ArrayList<>();

        final int BATCH_SIZE = 500;
        for (int start = 0; start < tokenStrings.size(); start += BATCH_SIZE) {
            List<String> batch = tokenStrings.subList(start, Math.min(start + BATCH_SIZE, tokenStrings.size()));

            MulticastMessage mm = MulticastMessage.builder()
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_notification")
                                    .setColor("#FF6B35")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build())
                    .addAllTokens(batch)
                    .build();

            try {
                BatchResponse br = firebaseMessaging.sendEachForMulticast(mm);
                successCount += br.getSuccessCount();
                failureCount += br.getFailureCount();

                List<SendResponse> rs = br.getResponses();
                for (int i = 0; i < rs.size(); i++) {
                    SendResponse r = rs.get(i);
                    FirebaseMessagingException fme = r.getException();
                    if (!r.isSuccessful()) {
                        var code = fme.getMessagingErrorCode();
                        if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                            invalidTokens.add(batch.get(i));
                        }
                        // 필요시 로깅
                    }
                }
            } catch (Exception e) {
                // 배치 단위 예외 → 해당 배치 실패 처리
                failureCount += batch.size();
            }
        }
        return new AlarmSendResult(successCount, failureCount, invalidTokens);
    }
}

