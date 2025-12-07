package boombimapi.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3Config {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName; // 버킷 이름 (필요하면 다른 빈에서 주입해서 사용)

    @Value("${cloud.aws.region.static}")
    private String bucketRegion; // 리전

    @Bean
    public S3Client s3Client() {
        log.info("[S3] Init S3Client. bucket={}, region={}", bucketName, bucketRegion);

        return S3Client.builder()
            .region(Region.of(bucketRegion))
            // EC2 인스턴스에 붙은 IAM Role(Instance Profile)을 통해 자격 증명 자동 사용
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
