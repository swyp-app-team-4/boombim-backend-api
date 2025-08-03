package boombim.domain.oauth2.presentation.controller;

import boombim.domain.oauth2.application.service.LoginLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class LoginLinkController {

    private final LoginLinkService loginLinkService;

    @GetMapping("/login")
    public ResponseEntity<String> loginPage() {
        return ResponseEntity.status(200).body(loginLinkService.getLoginLink());
    }
}
