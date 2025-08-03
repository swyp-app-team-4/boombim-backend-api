package boombim.domain.user.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "닉네임 수정")
public record NicknameReq(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Schema(description = "변경할 닉네임", example = "코딩왕선아")
        String nickname
) {
}
