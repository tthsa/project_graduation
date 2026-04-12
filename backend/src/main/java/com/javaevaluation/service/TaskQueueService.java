package com.javaevaluation.service;

import com.javaevaluation.config.RabbitMQConfig;
import com.javaevaluation.dto.CodeTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 任务队列服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskQueueService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送评测任务到队列
     */
    public void sendTask(CodeTask task) {
        log.info("发送评测任务: taskId={}, submissionId={}", task.getTaskId(), task.getSubmissionId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.TASK_ROUTING_KEY,
                task
        );
    }
}