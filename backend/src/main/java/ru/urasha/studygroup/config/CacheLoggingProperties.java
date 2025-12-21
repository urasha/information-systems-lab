package ru.urasha.studygroup.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cache")
public class CacheLoggingProperties {

    private boolean statsLoggingEnabled = false;
    private long statsLogThreshold = 0;
}
