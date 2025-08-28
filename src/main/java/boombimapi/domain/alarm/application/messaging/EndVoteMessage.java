package boombimapi.domain.alarm.application.messaging;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndVoteMessage {
    private Long voteId;
    private boolean isQuestioner;
}