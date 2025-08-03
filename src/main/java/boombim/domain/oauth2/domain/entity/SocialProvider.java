package boombim.domain.oauth2.domain.entity;

import boombim.global.infra.exception.error.BoombimException;

import java.util.Arrays;

public enum SocialProvider {
    KAKAO("kakao"),
    NAVER("naver"),
    APPLE("apple");

    private final String value;

    SocialProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SocialProvider from(String value) {
        return Arrays.stream(values())
                .filter(provider -> provider.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BoombimException(ErrorCode.INVALID_PROVIDER));
    }
}