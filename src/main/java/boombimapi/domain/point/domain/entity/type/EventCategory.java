package boombimapi.domain.point.domain.entity.type;

import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum EventCategory {

    EVENT_PARTICIPATION_TICKET("이벤트 응모권");

    private final String key;

    EventCategory(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    // Enum 매핑용 메서드
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EventCategory from(String key) {
        return Arrays.stream(EventCategory.values())
                .filter(r -> r.getKey().equalsIgnoreCase(key)) // 대소문자 구분 안함
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("이상한 타입이네요.: " + key));
    }

    public static EventCategory getByValue(String value) {
        for (EventCategory role : EventCategory.values()) {
            if (role.key.equals(value)) {
                return role;
            }
        }
        throw new BoombimException(ErrorCode.INVALID_ROLE);
    }
}
