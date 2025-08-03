package boombim.domain.oauth2.application.service;

import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;

public interface KakaoUserService {
    OAuth2UserResponse getUser(String accessToken);
}
