package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.cookie")
public record CookieProperties(
    boolean secure,
    String sameSite,
    String rtName,
    Long rtMaxAgeMillis,
    String path,
    String frontRedirect
) {

}
