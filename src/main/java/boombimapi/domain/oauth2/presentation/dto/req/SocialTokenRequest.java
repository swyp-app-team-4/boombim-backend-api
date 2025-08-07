package boombimapi.domain.oauth2.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 토큰 로그인 요청")
public record SocialTokenRequest(

        @Schema(description = "소셜 플랫폼에서 받은 액세스 토큰", example = "ya29.a0ARrdaM...")
        @NotBlank(message = "액세스 토큰은 필수입니다")
        String accessToken,

        @Schema(description = "소셜 플랫폼에서 받은 리프레시 토큰 (선택사항)", example = "1//0G...")
        String refreshToken,

        @Schema(description = "토큰 만료 시간 (초 단위)", example = "3600")
        Long expiresIn,

        @Schema(description = "Apple ID Token (Apple 로그인시에만 필요)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String idToken

) {
}