package boombimapi.global.infra.s3.presentation.application;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    // 프로필 바꾸기
    String storeUserProFile(MultipartFile multipartFile, String userId) throws IOException;

    // 정적 지도 화면 이미지 저장
    String storeStaticMapImage(
        String key,
        byte[] bytes,
        String contentType
    );

}
