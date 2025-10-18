package boombimapi.domain.oauth2.cookie.type;

import boombimapi.global.properties.CookieProperties;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public enum AuthCookieType {
    ACCESS(CookieProperties::atName, CookieProperties::atMaxAgeMillis),
    REFRESH(CookieProperties::rtName, CookieProperties::rtMaxAgeMillis);

    private final Function<CookieProperties, String> nameFunction;
    private final ToLongFunction<CookieProperties> maxAgeFunction;

    AuthCookieType(
        Function<CookieProperties, String> nameFunction,
        ToLongFunction<CookieProperties> maxAgeFunction
    ) {
        this.nameFunction = nameFunction;
        this.maxAgeFunction = maxAgeFunction;
    }

    public String nameFrom(
        CookieProperties properties
    ) {
        return nameFunction.apply(properties);
    }

    public long maxAgeFrom(
        CookieProperties properties
    ) {
        return maxAgeFunction.applyAsLong(properties);
    }
}
