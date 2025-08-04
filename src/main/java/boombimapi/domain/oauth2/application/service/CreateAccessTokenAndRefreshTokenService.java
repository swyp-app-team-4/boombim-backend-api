package boombimapi.domain.oauth2.application.service;

import boombimapi.domain.user.domain.entity.Role;

import java.util.Map;

public interface CreateAccessTokenAndRefreshTokenService {
    Map<String, String> createAccessTokenAndRefreshToken(String userId, Role role, String email);
}
