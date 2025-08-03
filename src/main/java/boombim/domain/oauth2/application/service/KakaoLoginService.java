package boombim.domain.oauth2.application.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface KakaoLoginService {
    String login(String code, HttpServletResponse response) throws IOException;
}
