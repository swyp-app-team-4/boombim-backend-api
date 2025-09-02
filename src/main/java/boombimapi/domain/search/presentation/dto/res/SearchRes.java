package boombimapi.domain.search.presentation.dto.res;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

public record SearchRes(
        Long placeId,
        String posName,
        LocalDateTime timeAt, // 가장 최신 날짜
        String answerType,
        String address,
        String imageUrl
) {
    public static SearchRes of(Long id,
                               String posName,
                               LocalDateTime timeAt,
                               String answerType,
                               String address,
                               String imageUrl) {
        return new SearchRes(
                id,
                posName,
                timeAt,
                answerType,
                address,
                imageUrl
        );
    }
}
