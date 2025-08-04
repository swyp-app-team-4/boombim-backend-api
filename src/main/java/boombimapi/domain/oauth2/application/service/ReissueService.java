package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.presentation.dto.response.LoginToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface ReissueService {
    LoginToken reissue(String refreshToken);
}
