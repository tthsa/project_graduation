package com.javaevaluation.service;

import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.entity.SubmissionHistory;
import com.javaevaluation.mapper.SubmissionMapper;
import com.javaevaluation.mapper.SubmissionHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeSubmitService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionHistoryMapper submissionHistoryMapper;
    private final ResultService resultService;
    private final TaskQueueService taskQueueService;

    /**
     * 提交作业
     */
    @Transactional
    public Integer submitHomework(Integer studentId, Integer homeworkId, List<String> filePaths) {
        // 1. 查询是否已有提交记录
        Submission existing = submissionMapper.findByHomeworkIdAndStudentId(homeworkId, studentId);

        if (existing != null) {
            // 2a. 保存历史记录
            SubmissionHistory history = new SubmissionHistory();
            history.setSubmissionId(existing.getId());
            history.setFilePaths(existing.getFilePaths());
            history.setSubmitTime(existing.getSubmitTime());
            submissionHistoryMapper.insert(history);

            // 2b. 更新提交记录
            existing.setFilePaths(filePaths.toArray(new String[0]));
            existing.setSubmitTime(LocalDateTime.now());
            existing.setStatus(0);  // 重置为待评测
            submissionMapper.update(existing);

            // 3. 发送评测任务
            sendEvaluationTask(existing);

            return existing.getId();
        } else {
            // 2c. 创建新提交记录
            Submission submission = new Submission();
            submission.setStudentId(studentId);
            submission.setHomeworkId(homeworkId);
            submission.setFilePaths(filePaths.toArray(new String[0]));
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus(0);  // 待评测
            submissionMapper.insert(submission);

            // 3. 发送评测任务
            sendEvaluationTask(submission);

            return submission.getId();
        }
    }

    /**
     * 发送评测任务
     */
    private void sendEvaluationTask(Submission submission) {
        CodeTask task = CodeTask.builder()
                .taskId(String.valueOf(submission.getId()))
                .submissionId(submission.getId())
                .studentId(submission.getStudentId())
                .homeworkId(submission.getHomeworkId())
                .filePaths(submission.getFilePaths())
                .timestamp(System.currentTimeMillis())
                .build();

        taskQueueService.sendTask(task);
        log.info("评测任务已发送: submissionId={}", submission.getId());
    }

    /**
     * 获取评测结果
     */
    public ExecutionResult getResult(Integer submissionId) {
        return resultService.getResult(String.valueOf(submissionId));
    }

    /**
     * 获取提交状态
     */
    public Integer getStatus(Integer submissionId) {
        Submission submission = submissionMapper.findById(submissionId);
        return submission != null ? submission.getStatus() : null;
    }

    /**
     * 更新提交状态
     */
    @Transactional
    public void updateStatus(Integer submissionId, Integer status) {
        Submission submission = new Submission();
        submission.setId(submissionId);
        submission.setStatus(status);
        submissionMapper.update(submission);
    }
}