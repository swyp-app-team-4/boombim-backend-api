package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.oauth2.presentation.dto.response.LoginToken;
import jakarta.servlet.http.HttpServletResponse;

public interface SocialLoginService {
    LoginToken login(SocialProvider provider, String code);
    String getLoginUrl(SocialProvider provider);
}