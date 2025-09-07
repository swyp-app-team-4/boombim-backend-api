package boombimapi.domain.oauth2.application.service;

public interface LogoutService {
    void logout(String userId, String refreshToken);
}