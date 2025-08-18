package boombimapi.domain.member.presentation.controller;

import boombimapi.domain.member.application.service.AdminService;
import boombimapi.domain.member.presentation.dto.admin.req.AdminLoginReq;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "관리자 로그인 API")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "[관리자 전용] 일반 로그인", description = "이메일과 비밀번호로 관리자 로그인을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)"),
            @ApiResponse(responseCode = "400", description = "잘못된 로그인 방식 (소셜 로그인 사용자)")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginToken> adminLogin(@Valid @RequestBody AdminLoginReq req) {
        LoginToken loginToken = adminService.postLogin(req);
        return ResponseEntity.ok(loginToken);
    }
}