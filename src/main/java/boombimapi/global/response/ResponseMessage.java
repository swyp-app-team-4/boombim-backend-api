package boombimapi.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    // official
    GET_OFFICIAL_PLACES_IN_VIEWPORT_SUCCESS("뷰포트 내 공식 장소 조회 성공"),
    GET_LATEST_OFFICIAL_CONGESTION_SUCCESS("최신 공식 혼잡도 조회 성공");

    private final String message;

}
