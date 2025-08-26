package boombimapi.global.infra.feignclient.naver.dto.res;

import java.util.List;

public record NaverImageSearchRes(
        List<Item> items
) {
    public record Item(
            String title,        // 이미지 제목 (HTML 태그 포함 가능)
            String link,         // 원본 이미지 URL
            String thumbnail,    // 썸네일 이미지 URL
            String sizeheight,   // 높이(px)
            String sizewidth     // 너비(px)
    ) {}
}