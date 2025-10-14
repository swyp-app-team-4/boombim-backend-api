package boombimapi.domain.point.domain.entity.type;

import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PointAction {
    EARN("적립"),
    USE("차감");

    private final String key;

    PointAction(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    // Enum 매핑용 메서드
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PointAction from(String key) {
        return Arrays.stream(PointAction.values())
                .filter(r -> r.getKey().equalsIgnoreCase(key)) // 대소문자 구분 안함
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("이상한 타입이네요.: " + key));
    }

    public static PointAction getByValue(String value) {
        for (PointAction role : PointAction.values()) {
            if (role.key.equals(value)) {
                return role;
            }
        }
        throw new BoombimException(ErrorCode.INVALID_ROLE);
    }
}
