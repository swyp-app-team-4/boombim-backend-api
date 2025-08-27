package boombimapi.global.infra.s3.presentation.application;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface S3Service {
    String storeUserProFile(MultipartFile multipartFile,  String userId) throws IOException;



}
