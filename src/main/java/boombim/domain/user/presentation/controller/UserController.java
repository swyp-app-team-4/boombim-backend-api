package boombim.domain.user.presentation.controller;

import boombim.domain.user.application.service.UserService;
import boombim.domain.user.presentation.dto.req.NicknameReq;
import boombim.domain.user.presentation.dto.res.GetUserRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "닉네임 수정 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "닉넴 수정", description = "사용자 닉네임을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 수정 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @PatchMapping
    public void updateNickname(@RequestBody @Valid NicknameReq req, @AuthenticationPrincipal String userId) {
        userService.updateNickname(userId, req);
    }


    @Operation(summary = "사용자 정보 조회 API ", description = "사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 존재하지 않음")
    })
    @GetMapping
    public ResponseEntity<GetUserRes> getUser(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

 
}