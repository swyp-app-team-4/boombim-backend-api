package boombimapi.domain.point.domain.entity.type;

import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PointCategory {
    CONGESTION("혼잡도 알리기"),
    EVENT("이벤트 응모");

    private final String key;

    PointCategory(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    // Enum 매핑용 메서드
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PointCategory from(String key) {
        return Arrays.stream(PointCategory.values())
                .filter(r -> r.getKey().equalsIgnoreCase(key)) // 대소문자 구분 안함
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("이상한 타입이네요.: " + key));
    }

    public static PointCategory getByValue(String value) {
        for (PointCategory role : PointCategory.values()) {
            if (role.key.equals(value)) {
                return role;
            }
        }
        throw new BoombimException(ErrorCode.INVALID_ROLE);
    }
}
