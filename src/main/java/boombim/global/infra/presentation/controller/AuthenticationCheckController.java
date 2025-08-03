package boombim.global.infra.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class AuthenticationCheckController {
    @GetMapping("/authcheck")
    @ResponseStatus(HttpStatus.OK)
    public void authcheck() {}

    @PostMapping("/test")
    public ResponseEntity<String> test(@AuthenticationPrincipal String userId) {
        log.info(userId);
        return ResponseEntity.ok("힝");
    }
}
