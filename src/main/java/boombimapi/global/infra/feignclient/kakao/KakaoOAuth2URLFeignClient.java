package boombimapi.global.infra.feignclient.kakao;

import boombimapi.domain.oauth2.presentation.dto.response.oatuh.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "KakaoOAuth",
        url = "https://kauth.kakao.com"
)
public interface KakaoOAuth2URLFeignClient {
    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoTokenResponse getAccessToken(
            @RequestParam("code") String code,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("grant_type") String grantType
    );

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoTokenResponse refreshToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("refresh_token") String refreshToken,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret
    );
}
