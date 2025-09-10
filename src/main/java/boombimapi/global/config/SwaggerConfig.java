package boombimapi.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "붐빔의 API 명세서",
                description = "API 명세서",
                version = "v1",
                contact = @Contact(
                        name = "고건호와 최승호",
                        email = "붐빔 이메일"
                )
        )
)
@Configuration
public class SwaggerConfig {

}
