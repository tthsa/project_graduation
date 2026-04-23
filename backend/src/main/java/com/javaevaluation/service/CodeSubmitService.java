package com.javaevaluation.service;

import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.entity.SubmissionFile;
import com.javaevaluation.entity.SubmissionHistory;
import com.javaevaluation.mapper.SubmissionFileMapper;
import com.javaevaluation.mapper.SubmissionHistoryMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 作业提交服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeSubmitService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionFileMapper submissionFileMapper;
    private final SubmissionHistoryMapper submissionHistoryMapper;
    private final ResultService resultService;
    private final TaskQueueService taskQueueService;

    /**
     * 提交作业
     * @param studentId 学生ID
     * @param homeworkId 作业ID
     * @param files 上传的文件数组
     * @return 提交ID
     */
    @Transactional
    public Integer submitHomework(Integer studentId, Integer homeworkId, MultipartFile[] files) throws IOException {
        // 1. 查询是否已有提交记录
        Submission existing = submissionMapper.findByHomeworkIdAndStudentId(homeworkId, studentId);

        if (existing != null) {
            // 2a. 保存历史记录
            SubmissionHistory history = new SubmissionHistory();
            history.setSubmissionId(existing.getId());
            history.setSubmitTime(existing.getSubmitTime());
            submissionHistoryMapper.insert(history);

            // 2b. 删除旧文件记录
            submissionFileMapper.deleteBySubmissionId(existing.getId());

            // 2c. 插入新文件记录
            List<SubmissionFile> newFiles = createSubmissionFiles(existing.getId(), files);
            for (SubmissionFile file : newFiles) {
                submissionFileMapper.insert(file);
            }

            // 2d. 更新提交记录
            existing.setSubmitTime(LocalDateTime.now());
            existing.setStatus(0);  // 重置为待评测
            submissionMapper.update(existing);

            // 3. 发送评测任务
            sendEvaluationTask(existing);

            return existing.getId();
        } else {
            // 2a. 创建新提交记录
            Submission submission = new Submission();
            submission.setStudentId(studentId);
            submission.setHomeworkId(homeworkId);
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus(0);  // 待评测
            submissionMapper.insert(submission);

            // 2b. 插入文件记录
            List<SubmissionFile> submissionFiles = createSubmissionFiles(submission.getId(), files);
            for (SubmissionFile file : submissionFiles) {
                submissionFileMapper.insert(file);
            }

            // 3. 发送评测任务
            sendEvaluationTask(submission);

            return submission.getId();
        }
    }

    /**
     * 创建提交文件列表
     */
    private List<SubmissionFile> createSubmissionFiles(Integer submissionId, MultipartFile[] files) throws IOException {
        List<SubmissionFile> submissionFiles = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmissionId(submissionId);
            submissionFile.setFileName(file.getOriginalFilename());
            submissionFile.setFileContent(content);
            submissionFile.setFileOrder(i);

            submissionFiles.add(submissionFile);
        }

        return submissionFiles;
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
                .timestamp(System.currentTimeMillis())
                .build();

        taskQueueService.sendTask(task);
        log.info("评测任务已发送: submissionId={}", submission.getId());
    }

    /**
     * 获取提交的文件列表
     */
    public List<SubmissionFile> getSubmissionFiles(Integer submissionId) {
        return submissionFileMapper.findBySubmissionId(submissionId);
    }

    /**
     * 获取评测结果
     */
    public EvaluationResult getResult(Integer submissionId) {
        return resultService.getResult(submissionId);
    }

    /**
     * 获取提交状态
     */
    public Integer getStatus(Integer submissionId) {
        Submission submission = submissionMapper.findById(submissionId);
        return submission != null ? submission.getStatus() : null;
    }
}