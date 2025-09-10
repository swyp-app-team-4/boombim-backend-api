package boombimapi.domain.oauth2.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 시 발급되는 액세스/리프레시 토큰")
public record LoginToken(

        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "접속해서 닉네임 수정 호출할지, 즉 false면 호출해야됨", example = "false, true")
        boolean nameFlag

) {
    public static LoginToken of(String accessToken, String refreshToken, boolean nameFlag) {
        return new LoginToken(accessToken, refreshToken, nameFlag);
    }
}
