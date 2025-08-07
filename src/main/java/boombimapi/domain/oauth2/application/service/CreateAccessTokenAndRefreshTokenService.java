package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import boombimapi.domain.user.domain.entity.Role;

public interface CreateAccessTokenAndRefreshTokenService {
    LoginToken createAccessTokenAndRefreshToken(String userId, Role role, String email);
}