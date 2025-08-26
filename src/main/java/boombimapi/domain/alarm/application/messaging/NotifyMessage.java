package boombimapi.domain.alarm.application.messaging;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyMessage {
    private Long alarmId;       // Alarm 엔티티 id
    private String title;
    private String body;
    private Integer retryCount; // 재시도 횟수
}