package boombimapi.global.config;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.global.config.converter.StringToSocialProviderConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToSocialProviderConverter stringToSocialProviderConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {

        // SocialProvider 대소문자 무시 변환
        registry.addConverter(String.class, SocialProvider.class, stringToSocialProviderConverter);
    }
}