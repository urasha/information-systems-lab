package ru.urasha.studygroup.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private boolean enabled = true;
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private boolean secure = false;
}
