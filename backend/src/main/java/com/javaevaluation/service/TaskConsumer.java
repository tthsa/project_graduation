package com.javaevaluation.service;

import com.javaevaluation.config.RabbitMQConfig;
import com.javaevaluation.dto.CodeTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ任务消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskConsumer {

    private final TaskProcessorService taskProcessor;

    /**
     * 消费评测任务
     */
    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void handleTask(CodeTask task) {
        log.info("📨 收到评测任务: taskId={}", task.getTaskId());

        try {
            taskProcessor.processTask(task);
            log.info("✅ 评测任务完成: taskId={}", task.getTaskId());
        } catch (Exception e) {
            log.error("❌ 评测任务失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
            // RabbitMQ会自动重试或进入死信队列
            throw e;
        }
    }
}