package com.codereview.backend.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "review")

public class ReviewProperties {

    /**Stream 配置*/
    private StreamConfig stream = new StreamConfig();

    /**结果配置*/
    private ResultConfig result = new ResultConfig();

    @Data
    public static class StreamConfig {
        /**任务流 Key*/
        private String key = "code:submit";

        /**消费者组名称*/
        private String group = "code-processor";
    }

    @Data
    public static class ResultConfig {
        /**结果流 Key*/
        private String stream = "code:result";

        /**结果 TTL（秒）*/
        private Long ttl = 86400L;
    }
}
