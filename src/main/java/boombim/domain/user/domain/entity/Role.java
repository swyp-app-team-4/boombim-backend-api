package boombim.domain.user.domain.entity;

import boombim.global.infra.exception.error.BoombimException;
import boombim.global.infra.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");
    private final String key;

    Role(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    // Enum 매핑용 메서드
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Role from(String key) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getKey().equalsIgnoreCase(key)) // 대소문자 구분 안함
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("등급이 없네요.: " + key));
    }

    public static Role getByValue(String value) {
        for (Role role : Role.values()) {
            if (role.key.equals(value)) {
                return role;
            }
        }
        throw new BoombimException(ErrorCode.INVALID_ROLE);
    }
}
