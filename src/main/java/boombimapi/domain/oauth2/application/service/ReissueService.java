package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;

public interface ReissueService {
    LoginToken reissue(String refreshToken);
}
