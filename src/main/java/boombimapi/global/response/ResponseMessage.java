package boombimapi.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    // official
    GET_OFFICIAL_PLACES_IN_VIEWPORT_SUCCESS("뷰포트 내 공식 장소 조회 성공"),
    GET_LATEST_OFFICIAL_CONGESTION_SUCCESS("최신 공식 혼잡도 조회 성공"),
    GET_OFFICIAL_PLACE_OVERVIEW_SUCCESS("공식 장소 개요 조회 성공"),

    // member place
    GET_MEMBER_PLACES_IN_VIEWPORT_SUCCESS("뷰포트 내 사용자 장소 조회 성공"),
    RESOLVE_MEMBER_PLACE_SUCCESS("사용자 장소 ID 조회 성공"),

    // member congestion
    CREATE_MEMBER_CONGESTION_SUCCESS("사용자 혼잡도 생성 성공"),

    // favorite
    ADD_FAVORITE_SUCCESS("즐겨찾기 추가 성공"),
    DELETE_FAVORITE_SUCCESS("즐겨찾기 삭제 성공"),
    GET_FAVORITES_SUCCESS("즐겨찾기 정보 리스트 조회 성공"),

    // vote
    VOTE_SUCCESS("투표 성공"),

    // mypage
    GET_MYPAGE_SUCCESS("마이페이지 조회 성공"),
    POST_PROFILE_SUCCESS("프로필 업로드 성공"),
    POST_NICKNAME_SUCCESS("닉네임 변경 성공"),
    MEMBER_DELETE("회원 탈퇴 성공"),
    LOGOUT_SUCCESS("로그아웃 성공"),

    // alarm
    GET_ALARM_SUCCESS("알림 성공");


    private final String message;

}
