package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.dto.HomeworkWithStatus;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.Homework;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.EvaluationResultMapper;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import com.javaevaluation.mapper.TestCaseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 作业控制器
 * 包含教师和学生的作业相关接口
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final TestCaseMapper testCaseMapper;
    private final EvaluationResultMapper evaluationResultMapper;

    // ==================== 教师接口 ====================

    /**
     * 获取所有作业列表（教师）
     */
    @GetMapping("/teacher/homework/list")
    public Result<List<Homework>> list() {
        List<Homework> homeworkList = homeworkMapper.findAll();
        return Result.success(homeworkList);
    }

    /**
     * 获取作业详情（教师）
     */
    @GetMapping("/teacher/homework/{id}")
    public Result<Homework> getById(@PathVariable Integer id) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(homework);
    }

    /**
     * 创建作业（教师）
     */
    @PostMapping("/teacher/homework/create")
    public Result<Homework> create(@Valid @RequestBody Homework homework) {
        homeworkMapper.insert(homework);
        return Result.success(homework);
    }

    /**
     * 更新作业（教师）
     */
    @PutMapping("/teacher/homework/{id}")
    public Result<Homework> update(@PathVariable Integer id, @Valid @RequestBody Homework homework) {
        Homework existingHomework = homeworkMapper.findById(id);
        if (existingHomework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        homework.setId(id);
        homeworkMapper.update(homework);
        return Result.success(homework);
    }

    /**
     * 删除作业（教师）
     */
    @DeleteMapping("/teacher/homework/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        homeworkMapper.delete(id);
        return Result.success(null);
    }

    // ==================== 学生接口 ====================

    /**
     * 获取作业列表（学生，带提交状态）
     */
    @GetMapping("/student/homework/list")
    public Result<List<HomeworkWithStatus>> listForStudent(@RequestParam Integer studentId) {
        List<Homework> homeworkList = homeworkMapper.findAll();
        List<HomeworkWithStatus> result = new ArrayList<>();

        for (Homework homework : homeworkList) {
            HomeworkWithStatus dto = new HomeworkWithStatus();
            dto.setHomework(homework);

            // 查询提交状态
            Submission submission = submissionMapper.findByHomeworkIdAndStudentId(homework.getId(), studentId);
            if (submission == null) {
                dto.setSubmitStatus(0); // 未提交
                dto.setScore(null);
            } else {
                dto.setSubmitStatus(submission.getStatus());
                dto.setSubmissionId(submission.getId());
                dto.setSubmitTime(submission.getSubmitTime());

                // 查询分数
                EvaluationResult evalResult = evaluationResultMapper.findBySubmissionId(submission.getId());
                if (evalResult != null) {
                    dto.setScore(evalResult.getTestScore());
                }
            }

            // 判断是否截止
            dto.setExpired(homework.getDeadline() != null && LocalDateTime.now().isAfter(homework.getDeadline()));

            result.add(dto);
        }

        return Result.success(result);
    }

    /**
     * 获取作业详情（学生）
     */
    @GetMapping("/student/homework/{id}")
    public Result<Homework> getByIdForStudent(@PathVariable Integer id) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(homework);
    }

    /**
     * 获取作业的公开测试用例（学生查看题目要求）
     */
    @GetMapping("/student/homework/{homeworkId}/testcases")
    public Result<List<TestCase>> getTestCasesForStudent(@PathVariable Integer homeworkId) {
        // 只返回公开的测试用例
        List<TestCase> testCases = testCaseMapper.findPublicByHomeworkId(homeworkId);
        return Result.success(testCases);
    }

    /**
     * 获取学生对某作业的提交状态
     */
    @GetMapping("/student/homework/{homeworkId}/status")
    public Result<Submission> getSubmissionStatus(
            @PathVariable Integer homeworkId,
            @RequestParam Integer studentId) {
        Submission submission = submissionMapper.findByHomeworkIdAndStudentId(homeworkId, studentId);
        return Result.success(submission);
    }
}