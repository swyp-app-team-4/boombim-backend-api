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



// package 예시: boombimapi.global.config.rabbitmq
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String EXCHANGE_PUSH = "push.direct";
    public static final String RK_PUSH_NOW   = "push.now";
    public static final String RK_PUSH_RETRY = "push.retry";
    public static final String Q_PUSH_NOW    = "push.now";
    public static final String Q_PUSH_RETRY  = "push.retry";

    // push.now: 워커가 소비
    @Bean
    public Queue pushNowQueue() {
        return QueueBuilder.durable(Q_PUSH_NOW).build();
    }

    // push.retry: 메시지가 여기서 TTL 끝나면 DLX로 넘어가 push.now로 재투입
    @Bean
    public Queue pushRetryQueue() {
        return QueueBuilder.durable(Q_PUSH_RETRY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_PUSH)
                .withArgument("x-dead-letter-routing-key", RK_PUSH_NOW)
                .build();
    }

    @Bean
    public DirectExchange pushExchange() {
        return new DirectExchange(EXCHANGE_PUSH, true, false);
    }

    @Bean
    public Binding bindPushNow() {
        return BindingBuilder.bind(pushNowQueue()).to(pushExchange()).with(RK_PUSH_NOW);
    }

    @Bean
    public Binding bindPushRetry() {
        return BindingBuilder.bind(pushRetryQueue()).to(pushExchange()).with(RK_PUSH_RETRY);
    }

    // JSON 변환기 (DTO ↔️ 메시지)
    @Bean
    public MessageConverter jackson2MessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        // 퍼블리셔 컨펌(선택) - 발행 성공/실패 로깅
        template.setMandatory(true);
        return template;
    }

    // 워커 prefetch 튜닝
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(connectionFactory);
        f.setMessageConverter(converter);
        f.setPrefetchCount(200); // 한번에 가져올 메시지 수
        f.setConcurrentConsumers(4);
        f.setMaxConcurrentConsumers(8);
        return f;
    }
}

