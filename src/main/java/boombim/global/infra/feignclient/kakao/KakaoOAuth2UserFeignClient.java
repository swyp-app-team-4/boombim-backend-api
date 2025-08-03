package boombim.global.infra.feignclient.kakao;


import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "KakaoOAuth2UserInfo",
        url = "https://kapi.kakao.com"
)
public interface KakaoOAuth2UserFeignClient {

    @GetMapping(value = "/v2/user/me")
    OAuth2UserResponse getUserInfo(@RequestHeader("Authorization") String accessToken);

}
