package boombim.global.infra.feignclient.naver;

import boombim.domain.oauth2.presentation.dto.response.naver.NaverUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "NaverOAuth2UserInfo", url = "https://openapi.naver.com")
public interface NaverOAuth2UserFeignClient {

    @GetMapping("/v1/nid/me")
    NaverUserResponse getUserInfo(@RequestHeader("Authorization") String accessToken);
}