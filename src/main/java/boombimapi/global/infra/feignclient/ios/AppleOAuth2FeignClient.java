package boombimapi.global.infra.feignclient.ios;

import boombimapi.domain.oauth2.presentation.dto.response.apple.AppleTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "AppleOAuth",
        url = "https://appleid.apple.com",
        configuration = AppleOAuth2FeignConfig.class
)
public interface AppleOAuth2FeignClient {

    @PostMapping(
            value = "/auth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    AppleTokenResponse getAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("redirect_uri") String redirectUri
    );

    @PostMapping(
            value = "/auth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    AppleTokenResponse refreshToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("refresh_token") String refreshToken
    );
}