package com.javaevaluation.service;

import com.javaevaluation.config.RabbitMQConfig;
import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.entity.Homework;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评测触发服务
 * 教师手动触发评测的入口。学生提交不再自动发 MQ,由此服务统一调度。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final SubmissionMapper submissionMapper;
    private final HomeworkMapper homeworkMapper;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 触发单条提交评测
     *
     * 状态规则:
     *   0 (待评测) → 触发
     *   3 (失败)   → 触发(重评)
     *   1 (评测中) → 跳过
     *   2 (已完成) → 跳过(不可重评)
     *
     * @return true=已发 MQ;false=状态不允许或提交不存在
     */
    public boolean triggerOne(Integer submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            log.warn("触发评测失败: submission 不存在, id={}", submissionId);
            return false;
        }
        return triggerIfEligible(submission);
    }

    /**
     * 批量触发某作业下所有待评测/失败的提交
     * @return {triggered: 已触发数, skipped: 跳过数}
     */
    public Map<String, Integer> triggerBatch(Integer homeworkId) {
        List<Submission> submissions = submissionMapper.findByHomeworkId(homeworkId);
        int triggered = 0;
        int skipped = 0;
        for (Submission s : submissions) {
            if (triggerIfEligible(s)) {
                triggered++;
            } else {
                skipped++;
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("triggered", triggered);
        result.put("skipped", skipped);
        log.info("批量评测触发: homeworkId={}, triggered={}, skipped={}", homeworkId, triggered, skipped);
        return result;
    }

    private boolean triggerIfEligible(Submission submission) {
        Integer status = submission.getStatus();
        if (status == null || (status != 0 && status != 3)) {
            return false;
        }
        sendTaskForSubmission(submission);
        submissionMapper.updateStatus(submission.getId(), 1);
        return true;
    }

    private void sendTaskForSubmission(Submission submission) {
        Homework homework = homeworkMapper.selectById(submission.getHomeworkId());
        CodeTask task = CodeTask.builder()
                .taskId(String.valueOf(submission.getId()))
                .submissionId(submission.getId())
                .studentId(submission.getStudentId())
                .homeworkId(submission.getHomeworkId())
                .homeworkTitle(homework != null ? homework.getTitle() : null)
                .homeworkDescription(homework != null ? homework.getDescription() : null)
                .timestamp(System.currentTimeMillis())
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.TASK_ROUTING_KEY,
                task
        );
        log.info("评测任务已发送: submissionId={}, homeworkTitle={}", submission.getId(), task.getHomeworkTitle());
    }
}
