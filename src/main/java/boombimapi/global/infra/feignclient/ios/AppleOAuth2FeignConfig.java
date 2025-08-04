package boombimapi.global.infra.feignclient.ios;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class AppleOAuth2FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                10000,  // 연결 타임아웃 (밀리초)
                30000   // 읽기 타임아웃 (밀리초)
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new AppleOAuth2ErrorDecoder();
    }

    @Slf4j
    public static class AppleOAuth2ErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            log.error("Apple OAuth2 API 에러 - Method: {}, Status: {}, Reason: {}",
                    methodKey, response.status(), response.reason());

            if (response.status() == 400) {
                return new RuntimeException("Apple OAuth2 잘못된 요청: " + response.reason());
            } else if (response.status() == 401) {
                return new RuntimeException("Apple OAuth2 인증 실패: " + response.reason());
            } else if (response.status() >= 500) {
                return new RuntimeException("Apple 서버 오류: " + response.reason());
            }

            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}