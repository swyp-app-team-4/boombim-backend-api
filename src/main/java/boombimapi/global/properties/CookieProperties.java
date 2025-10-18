package boombimapi.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.cookie")
public record CookieProperties(
    boolean secure,
    String sameSite,
    String atName,
    String rtName,
    Long atMaxAgeMillis,
    Long rtMaxAgeMillis,
    String path,
    String frontRedirect
) {

}
