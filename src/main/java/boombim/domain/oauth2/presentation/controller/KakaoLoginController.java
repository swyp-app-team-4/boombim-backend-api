package boombim.domain.oauth2.presentation.controller;

import boombim.domain.oauth2.application.service.KakaoLoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginController {

    private final KakaoLoginService KakaoLoginService;

    @GetMapping("/callback")
    public ResponseEntity<Void>  login(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        String redirectUrl = KakaoLoginService.login(code, response);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();

    }

}
