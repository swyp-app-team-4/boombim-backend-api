package boombimapi.global.infra.feignclient.kakao;


import boombimapi.domain.oauth2.presentation.dto.res.oatuh.KakaoUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "KakaoOAuth2UserInfo",
        url = "https://kapi.kakao.com"
)
public interface KakaoOAuth2UserFeignClient {


    @GetMapping(value = "/v2/user/me")
    KakaoUserResponse getUserInfo(@RequestHeader("Authorization") String accessToken);

}
