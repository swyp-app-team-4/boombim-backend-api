package boombimapi.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String EXCHANGE_PUSH = "push.direct";

    // === 공지 알림 ===
    public static final String RK_PUSH_NOW   = "push.now";
    public static final String RK_PUSH_RETRY = "push.retry";
    public static final String Q_PUSH_NOW    = "push.now";
    public static final String Q_PUSH_RETRY  = "push.retry";

    // === 투표 종료 알림 ===
    public static final String RK_END_VOTE   = "push.endvote";
    public static final String Q_END_VOTE    = "push.endvote";

    // --- 큐 정의 ---

    // 공지 즉시 큐
    @Bean
    public Queue pushNowQueue() {
        return QueueBuilder.durable(Q_PUSH_NOW).build();
    }

    // 공지 재시도 큐
    @Bean
    public Queue pushRetryQueue() {
        return QueueBuilder.durable(Q_PUSH_RETRY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_PUSH)
                .withArgument("x-dead-letter-routing-key", RK_PUSH_NOW)
                .build();
    }

    // 투표 종료 큐
    @Bean
    public Queue endVoteQueue() {
        return QueueBuilder.durable(Q_END_VOTE).build();
    }

    // --- 익스체인지 ---
    @Bean
    public DirectExchange pushExchange() {
        return new DirectExchange(EXCHANGE_PUSH, true, false);
    }

    // --- 바인딩 ---
    @Bean
    public Binding bindPushNow() {
        return BindingBuilder.bind(pushNowQueue()).to(pushExchange()).with(RK_PUSH_NOW);
    }

    @Bean
    public Binding bindPushRetry() {
        return BindingBuilder.bind(pushRetryQueue()).to(pushExchange()).with(RK_PUSH_RETRY);
    }

    @Bean
    public Binding bindEndVote() {
        return BindingBuilder.bind(endVoteQueue()).to(pushExchange()).with(RK_END_VOTE);
    }

    // --- 메시지 컨버터 ---
    @Bean
    public MessageConverter jackson2MessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        template.setMandatory(true); // 발행 성공/실패 로그
        return template;
    }

    // --- Listener 컨테이너 튜닝 ---
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(connectionFactory);
        f.setMessageConverter(converter);
        f.setPrefetchCount(200);
        f.setConcurrentConsumers(4);
        f.setMaxConcurrentConsumers(8);
        return f;
    }
}
