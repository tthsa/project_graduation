package com.javaevaluation.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "review")
public class ReviewProperties {

    /**RabbitMQ 配置*/
    private RabbitConfig rabbit = new RabbitConfig();

    /**结果配置*/
    private ResultConfig result = new ResultConfig();

    @Data
    public static class RabbitConfig {
        /**任务队列名称*/
        private String taskQueue = "evaluation.task.queue";

        /**结果队列名称*/
        private String resultQueue = "evaluation.result.queue";

        /**交换机名称*/
        private String exchange = "evaluation.exchange";
    }

    @Data
    public static class ResultConfig {
        /**结果 TTL（秒）*/
        private Long ttl = 86400L;
    }
}