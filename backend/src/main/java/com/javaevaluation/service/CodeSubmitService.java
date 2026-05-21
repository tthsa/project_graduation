package com.javaevaluation.service;

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

    /**
     * 提交作业
     * @param studentId 学生ID
     * @param homeworkId 作业ID
     * @param files 上传的文件数组
     * @return 提交ID
     */
    @Transactional
    public Integer submitHomework(Integer studentId, Integer homeworkId, MultipartFile[] files) throws IOException {
        Submission submission = submissionMapper.findByHomeworkIdAndStudentId(homeworkId, studentId);

        if (submission != null) {
            // 复用已有提交：保存历史、重置状态
            SubmissionHistory history = new SubmissionHistory();
            history.setSubmissionId(submission.getId());
            history.setSubmitTime(submission.getSubmitTime());
            submissionHistoryMapper.insert(history);

            submissionFileMapper.deleteBySubmissionId(submission.getId());
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus(0);
            submissionMapper.updateById(submission);
        } else {
            // 新建提交
            submission = new Submission();
            submission.setStudentId(studentId);
            submission.setHomeworkId(homeworkId);
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus(0);
            submissionMapper.insert(submission);
        }

        // 插入文件记录(评测由教师手动触发,这里不再自动发 MQ)
        for (SubmissionFile file : createSubmissionFiles(submission.getId(), files)) {
            submissionFileMapper.insert(file);
        }
        return submission.getId();
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
        Submission submission = submissionMapper.selectById(submissionId);
        return submission != null ? submission.getStatus() : null;
    }
}