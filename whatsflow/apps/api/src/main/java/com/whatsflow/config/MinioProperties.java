package com.whatsflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "whatsflow.minio")
public class MinioProperties {

    private String endpoint = "http://localhost:9000";
    private String accessKey;
    private String secretKey;
    private String bucket = "whatsflow";
    private boolean secure = false;
}
