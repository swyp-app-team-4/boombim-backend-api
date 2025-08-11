package boombimapi.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    GET_OFFICIAL_PLACES_WITHIN_VIEWPORT_SUCCESS("뷰포트 내 공식 장소 마커 조회 성공");

    private final String message;

}
