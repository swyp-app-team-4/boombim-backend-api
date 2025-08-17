package boombimapi.domain.member.presentation.dto.member.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 호출 첫 화면")
public record GetNicknameRes(

        @Schema(description = "닉네임 처음에 호출안됐으면 False 됐으면 true", example = "true")
        boolean nameFlag

) {
    public static GetNicknameRes of(boolean nameFlag) {
        return new GetNicknameRes(
                nameFlag
        );
    }
}