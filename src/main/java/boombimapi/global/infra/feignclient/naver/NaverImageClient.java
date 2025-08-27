package boombimapi.global.infra.feignclient.naver;

import boombimapi.global.infra.feignclient.naver.dto.res.NaverImageSearchRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "naverImageClient",
        url  = "https://openapi.naver.com",
        configuration = boombimapi.global.config.NaverFeignConfig.class
)
public interface NaverImageClient {

    /**  이미지 검색 (JSON) */
    @GetMapping(value = "/v1/search/image", produces = "application/json")
    NaverImageSearchRes searchImages(
            @RequestParam("query")   String query,                         // 검색어(필수)
            @RequestParam("display") int display,                          // 최대 100
            @RequestParam("start")   int start,                            // 1 이상
            @RequestParam("sort")    String sort,                          // sim | date
            @RequestParam(value = "filter", defaultValue = "all") String filter  // all|large|medium|small
    );
}
