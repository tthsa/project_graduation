package com.javaevaluation.service;

import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.dto.ExecutionResult;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param filePaths 文件路径列表
     * @return 提交ID
     */
    @Transactional
    public Integer submitHomework(Integer studentId, Integer homeworkId, List<String> filePaths) {
        // 1. 查询是否已有提交记录
        Submission existing = submissionMapper.findByHomeworkIdAndStudentId(homeworkId, studentId);

        if (existing != null) {
            // 2a. 保存历史记录
            SubmissionHistory history = new SubmissionHistory();
            history.setSubmissionId(existing.getId());
            history.setSubmitTime(existing.getSubmitTime());
            submissionHistoryMapper.insert(history);

            // 2b. 复制当前文件到历史记录
            List<SubmissionFile> currentFiles = submissionFileMapper.findBySubmissionId(existing.getId());
            for (SubmissionFile file : currentFiles) {
                SubmissionFile historyFile = new SubmissionFile();
                historyFile.setSubmissionId(history.getId());  // 关联到历史记录
                historyFile.setFilePath(file.getFilePath());
                historyFile.setFileName(file.getFileName());
                historyFile.setFileSize(file.getFileSize());
                historyFile.setUploadTime(file.getUploadTime());
                // 注意：这里需要修改，历史文件应该关联到 submission_history
                // 暂时先删除旧文件，后续可以优化
            }

            // 2c. 删除旧文件记录
            submissionFileMapper.deleteBySubmissionId(existing.getId());

            // 2d. 插入新文件记录
            List<SubmissionFile> newFiles = createSubmissionFiles(existing.getId(), filePaths);
            submissionFileMapper.batchInsert(newFiles);

            // 2e. 更新提交记录
            existing.setSubmitTime(LocalDateTime.now());
            existing.setStatus(0);  // 重置为待评测
            submissionMapper.update(existing);

            // 3. 发送评测任务
            sendEvaluationTask(existing, filePaths);

            return existing.getId();
        } else {
            // 2c. 创建新提交记录
            Submission submission = new Submission();
            submission.setStudentId(studentId);
            submission.setHomeworkId(homeworkId);
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus(0);  // 待评测
            submissionMapper.insert(submission);

            // 3. 插入文件记录
            List<SubmissionFile> files = createSubmissionFiles(submission.getId(), filePaths);
            submissionFileMapper.batchInsert(files);

            // 4. 发送评测任务
            sendEvaluationTask(submission, filePaths);

            return submission.getId();
        }
    }

    /**
     * 创建提交文件列表
     */
    private List<SubmissionFile> createSubmissionFiles(Integer submissionId, List<String> filePaths) {
        LocalDateTime now = LocalDateTime.now();
        List<SubmissionFile> files = new ArrayList<>();

        for (String path : filePaths) {
            SubmissionFile file = new SubmissionFile();
            file.setSubmissionId(submissionId);
            file.setFilePath(path);
            file.setFileName(extractFileName(path));
            file.setFileSize(0L);  // 文件大小可以后续补充
            file.setUploadTime(now);
            files.add(file);
        }

        return files;
    }

    /**
     * 从路径中提取文件名
     */
    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            lastSlash = path.lastIndexOf('\\');
        }
        return lastSlash == -1 ? path : path.substring(lastSlash + 1);
    }

    /**
     * 发送评测任务
     */
    private void sendEvaluationTask(Submission submission, List<String> filePaths) {
        CodeTask task = CodeTask.builder()
                .taskId(String.valueOf(submission.getId()))
                .submissionId(submission.getId())
                .studentId(submission.getStudentId())
                .homeworkId(submission.getHomeworkId())
                .filePaths(filePaths.toArray(new String[0]))
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