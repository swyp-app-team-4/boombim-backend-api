package boombimapi.domain.member.presentation.dto.admin.req;

public record AdminLoginReq(
        String loginId,

        String password
) {
}
