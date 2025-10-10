package boombimapi.domain.oauth2.presentation.controller;

import boombimapi.domain.member.application.service.MemberService;
import boombimapi.domain.member.presentation.dto.member.res.GetMemberRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.response.BaseResponse;
import boombimapi.global.response.ResponseMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web/auth")
@Tag(name = "Auth (Web)", description = "웹에서 로그인 시 주입된 쿠키 확인을 위한 엔드포인트")
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<GetMemberRes>> me(
        @AuthenticationPrincipal String memberId
    ) {
        if (memberId == null) {
            throw new BoombimException(ErrorCode.UNAUTHORIZED);
        }

        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                ResponseMessage.GET_MEMBER_INFO_SUCCESS,
                memberService.getMember(memberId)
            )
        );
    }

}
