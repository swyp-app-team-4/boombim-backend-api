package boombimapi.global.infra.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 에러코드 규약
     * HTTP Status Code는 에러에 가장 유사한 코드를 부여한다.
     * 사용자정의 에러코드는 음수를 사용한다.
     * 사용자정의 에러코드는 중복되지 않게 배정한다.
     * 사용자정의 에러코드는 각 카테고리 별로 100단위씩 끊어서 배정한다. 단, Common 카테고리는 -100 단위를 고정으로 가져간다.
     */

    /**
     * 401 : 미승인
     * 403 : 권한의 문제가 있을때
     * 406 : 객체가 조회되지 않을 때
     * 409 : 현재 데이터와 값이 충돌날 때(ex. 아이디 중복)
     * 412 : 파라미터 값이 뭔가 누락됐거나 잘못 왔을 때
     * 422 : 파라미터 문법 오류
     * 424 : 뭔가 단계가 꼬였을때, 1번안하고 2번하고 그런경우
     */

    // Common
    SERVER_UNTRACKED_ERROR(-100, "미등록 서버 에러입니다. 서버 팀에 연락주세요.", 500),
    OBJECT_NOT_FOUND(-101, "조회된 객체가 없습니다.", 406),
    INVALID_PARAMETER(-102, "잘못된 파라미터입니다.", 422),
    PARAMETER_VALIDATION_ERROR(-103, "파라미터 검증 에러입니다.", 422),
    PARAMETER_GRAMMAR_ERROR(-104, "파라미터 문법 에러입니다.", 422),

    //Auth
    UNAUTHORIZED(-200, "인증 자격이 없습니다.", 401),
    FORBIDDEN(-201, "권한이 없습니다.", 403),
    ID_ERROR_TOKEN(-202, "잘못된 ID 토큰입니다.", 401),
    APPLE_ERROR_KEY(-202, "키 파싱 실패입니다.", 401),
    JWT_ERROR_TOKEN(-202, "잘못된 토큰입니다.", 401),
    JWT_EXPIRE_TOKEN(-203, "만료된 토큰입니다.", 401),
    AUTHORIZED_ERROR(-204, "인증 과정 중 에러가 발생했습니다.", 500),
    INVALID_ACCESS_TOKEN(-205, "Access Token이 유효하지 않습니다.", 401),
    JWT_UNMATCHED_CLAIMS(-206, "토큰 인증 정보가 일치하지 않습니다", 401),
    INVALID_REFRESH_TOKEN(-207, "Refresh Token이 유효하지 않습니다.", 401),
    REFRESH_TOKEN_NOT_EXIST(-208, "Refresh Token이 DB에 존재하지 않습니다.", 401),
    DUPLICATE_LOGIN_NOT_EXIST(-209, "중복 로그인은 허용되지 않습니다.", 401),

    // OAuth2
    INVALID_PROVIDER(-220, "지원하지 않는 소셜 로그인 제공자입니다.", 400),
    APPLE_JWT_ERROR(-221, "Apple JWT 생성 중 오류가 발생했습니다.", 500),
    UNSUPPORTED_PROVIDER(-222, "지원하지 않는 OAuth2 제공자입니다.", 400),

    // user
    INVALID_ROLE(-210, "해당 역할이 존재하지 않습니다.", 400),
    USER_NOT_EXIST(-211, "존재하지 않는 유저입니다.", 404),
    DUPLICATE_EMAIL(-213, "이미 사용 중인 이메일입니다.", 409),

    // Alarm & FCM
    FCM_TOKEN_REGISTER_FAILED(-300, "FCM 토큰 등록에 실패했습니다.", 500),
    FCM_SEND_FAILED(-301, "알림 전송에 실패했습니다.", 500),
    ALARM_NOT_FOUND(-302, "알림을 찾을 수 없습니다.", 404),
    ALARM_ACCESS_DENIED(-303, "해당 알림에 대한 권한이 없습니다.", 403),
    FIREBASE_INITIALIZATION_FAILED(-304, "Firebase 초기화에 실패했습니다.", 500),
    INVALID_DEVICE_TYPE(-305, "유효하지 않은 디바이스 타입입니다.", 400),

    // official congestion
    OFFICIAL_CONGESTION_NOT_FOUND(-400, "공식 혼잡도 데이터가 존재하지 않습니다.", 406);
    private final int code;
    private final String message;
    private final int httpCode;
}