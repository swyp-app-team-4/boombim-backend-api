package boombimapi.domain.member.presentation.dto.member.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 이유")
public record MemberLeaveReq(
        @Schema(description = "탈퇴 이유", example = "신규 계정으로 가입할래요")
        String leaveReason
) {
}
