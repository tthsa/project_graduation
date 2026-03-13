package com.codereview.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "siliconflow")

public class SiliconFlowProperties {
    private String apiKey;
    private String baseUrl = "https://api.siliconflow.cn/v1";
    private String model = "deepseek-ai/DeepSeek-V3";
    private Integer timeout = 60000;
}
