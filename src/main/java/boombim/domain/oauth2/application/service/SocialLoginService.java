package boombim.domain.oauth2.application.service;

import boombim.domain.oauth2.domain.entity.SocialProvider;
import jakarta.servlet.http.HttpServletResponse;

public interface SocialLoginService {
    String login(SocialProvider provider, String code, HttpServletResponse response);
    String getLoginUrl(SocialProvider provider);
}