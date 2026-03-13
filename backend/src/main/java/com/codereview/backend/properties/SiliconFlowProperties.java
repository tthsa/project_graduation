package com.codereview.backend.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "siliconflow")

public class SiliconFlowProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
    private Integer timeout = 60000;
}
