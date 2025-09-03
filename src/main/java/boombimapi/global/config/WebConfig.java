package boombimapi.global.config;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.global.config.converter.StringToSocialProviderConverter;
import boombimapi.global.properties.ClovaCongestionMessageProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ClovaCongestionMessageProperties congestionMessageProperties;
    private final StringToSocialProviderConverter stringToSocialProviderConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {

        // SocialProvider 대소문자 무시 변환
        registry.addConverter(String.class, SocialProvider.class, stringToSocialProviderConverter);
    }

    @Bean(name = "clovaCongestionMessageWebClient")
    public WebClient clovaCongestionMessageClient(
        WebClient.Builder builder
    ) {
        return builder
            .baseUrl(congestionMessageProperties.baseUrl())
            .defaultHeaders(header -> {
                header.setBearerAuth(congestionMessageProperties.apiKey());
                header.setContentType(MediaType.APPLICATION_JSON);
                header.setAccept(List.of(MediaType.APPLICATION_JSON));
            })
            .build();
    }

}