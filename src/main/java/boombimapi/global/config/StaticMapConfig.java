package boombimapi.global.config;

import boombimapi.global.properties.StaticMapProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class StaticMapConfig {

    @Bean
    @Qualifier("naverStaticMapHttpClient")
    public WebClient naverStaticMapHttpClient(
        WebClient.Builder builder,
        StaticMapProperties properties
    ) {
        HttpClient http = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
            .doOnConnected(
                connection -> connection
                    .addHandlerLast(new ReadTimeoutHandler(5))
                    .addHandlerLast(new WriteTimeoutHandler(5))
            );

        return builder
            .baseUrl(properties.baseUrl())
            .defaultHeader("x-ncp-apigw-api-key-id", properties.apiKeyId())
            .defaultHeader("x-ncp-apigw-api-key", properties.apiKey())
            .clientConnector(new ReactorClientHttpConnector(http))
            .build();
    }

}
