package boombimapi.domain.region.presentation.dto.res;

import jakarta.persistence.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegionRes(
        LocalDate regionDate,        // 날짜
        LocalDateTime startTime, // 시작 시간
        LocalDateTime endTime,   // 끝나는 시간
        String posName,          // 장소
        Long peopleCnt           // 인원 수
) {
    // 정적 팩토리 메서드
    public static RegionRes of(
            LocalDate regionDate,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String posName,
            Long peopleCnt
    ) {
        return new RegionRes(regionDate, startTime, endTime, posName, peopleCnt);
    }
}
