package boombimapi.domain.alarm.infra.messaging;

import boombimapi.domain.alarm.application.messaging.EndVoteMessage;
import boombimapi.domain.alarm.application.messaging.NotifyMessage;
import boombimapi.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PushProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishNow(NotifyMessage msg) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PUSH,
                RabbitMQConfig.RK_PUSH_NOW,
                msg
        );
    }

    public void publishEndVote(EndVoteMessage msg) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PUSH,
                RabbitMQConfig.RK_END_VOTE,
                msg
        );
    }
    // 재시도 큐로 지연 발행 (per-message TTL)
    public void publishRetry(NotifyMessage msg, long delayMillis) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PUSH,
                RabbitMQConfig.RK_PUSH_RETRY,
                msg,
                m -> {
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return m;
                }
        );
    }
}
