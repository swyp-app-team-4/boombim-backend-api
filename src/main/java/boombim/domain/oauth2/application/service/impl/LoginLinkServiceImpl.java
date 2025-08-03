package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.LoginLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoginLinkServiceImpl implements LoginLinkService {

    @Value("${oauth2.base-url}")
    private String baseUrl;

    @Value("${oauth2.client-id}")
    private String clientId;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public String getLoginLink() {
        return baseUrl +
                "?client_id=" +
                clientId +
                "&redirect_uri=" +
                redirectUri +
                "&response_type=code" +
                "&scope=profile_nickname,profile_image,account_email";  // 필요한 Scope를 콤마로 구분
    }


}