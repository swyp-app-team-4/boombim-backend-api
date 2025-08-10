package boombimapi.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    GET_ALL_OFFICIAL_PLACES_CENTER_SUCCESS("모든 공식 장소 중앙 좌표 조회 성공");

    private final String message;

}
