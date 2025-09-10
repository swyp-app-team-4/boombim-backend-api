package boombimapi.domain.search.presentation.dto.res;

import java.time.LocalDateTime;

import boombimapi.domain.place.entity.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검색 결과 응답 모델")
public record SearchRes(

        @Schema(description = "장소 ID", example = "12345")
        Long placeId,

        @Schema(description = "장소명", example = "스타벅스 강남점")
        String posName,

        @Schema(description = "가장 최신 날짜 그 몇분전 설명", example = "2024-12-01T14:30:00")
        LocalDateTime timeAt,

        @Schema(description = "최신 답변 유형", example = "여유", allowableValues = {"여유", "보통", "약간 붐빔", "붐빔"})
        String answerType,

        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
        String address,

        @Schema(description = "이미지 URL // 원래 공식만 주는게 맞는데 나중을 사용자도 올린거 다 주겠음", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "장소 유형", example = "OFFICIAL_PLACE / MEMBER_PLACE")
        PlaceType placeType,
        @Schema(description = "즐겨찾기 여부 true면 한거 false면 안한거", example = "true or false")
        boolean favoriteFlag

) {
    @Schema(description = "SearchRes 객체 생성을 위한 팩토리 메서드")
    public static SearchRes of(Long id, String posName, LocalDateTime timeAt,
                               String answerType, String address, String imageUrl, PlaceType placeType, boolean favoriteFlag) {
        return new SearchRes(
                id,
                posName,
                timeAt,
                answerType,
                address,
                imageUrl,
                placeType,
                favoriteFlag
        );
    }
}