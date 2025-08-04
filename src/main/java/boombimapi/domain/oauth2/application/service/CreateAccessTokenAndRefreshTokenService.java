package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.oauth2.presentation.dto.response.LoginToken;
import boombimapi.domain.user.domain.entity.Role;

import java.util.Map;

public interface CreateAccessTokenAndRefreshTokenService {
    LoginToken createAccessTokenAndRefreshToken(String userId, Role role, String email);
}