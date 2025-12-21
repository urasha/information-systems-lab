package ru.urasha.studygroup.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MinioConfiguration.class);

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();
    }

    @PostConstruct
    public void ensureBucketExists() {
        if (!properties.isEnabled()) {
            log.info("MinIO integration disabled via configuration");
            return;
        }
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("Created MinIO bucket '{}'", properties.getBucket());
            }
        } catch (Exception ex) {
            log.warn("Failed to verify/create MinIO bucket '{}': {}", properties.getBucket(), ex.getMessage());
        }
    }
}
