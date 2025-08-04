package boombimapi.domain.oauth2.domain.entity;

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
}