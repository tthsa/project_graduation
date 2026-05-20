package com.javaevaluation.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.dto.HomeworkWithStatus;
import com.javaevaluation.dto.LlmDimension;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.Homework;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.EvaluationResultMapper;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import com.javaevaluation.mapper.TestCaseMapper;
import com.javaevaluation.security.JwtUtils;
import com.javaevaluation.service.HomeworkOwnershipService;
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
    private final JwtUtils jwtUtils;
    private final HomeworkOwnershipService homeworkOwnershipService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        String error = validateScoringConfig(homework);
        if (error != null) {
            return Result.fail(400, error);
        }
        homeworkMapper.insert(homework);
        return Result.success(homework);
    }

    /**
     * 更新作业（教师）
     */
    @PutMapping("/teacher/homework/{id}")
    public Result<Homework> update(
            @PathVariable Integer id,
            @Valid @RequestBody Homework homework,
            @RequestHeader("Authorization") String authHeader) {
        Homework existingHomework = homeworkMapper.findById(id);
        if (existingHomework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        Integer teacherId = currentUserId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        if (!homeworkOwnershipService.isHomeworkOwnedBy(id, teacherId)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        String error = validateScoringConfig(homework);
        if (error != null) {
            return Result.fail(400, error);
        }
        homework.setId(id);
        homeworkMapper.update(homework);
        return Result.success(homework);
    }

    /**
     * 校验老师配置的打分规则。返回 null 表示通过，否则返回错误信息。
     * - 测试权重 + LLM 权重 = 100（若任一非空,则两个都必须非空且和为 100）
     * - 等级阈值: A > B > C >= 0 且 A <= 100
     * - 权重 [0, 100]
     * - llmDimensions JSON: 数组, 每项 {name, weight}, weight 之和 = 100
     */
    private String validateScoringConfig(Homework hw) {
        Integer tw = hw.getTestWeight();
        Integer lw = hw.getLlmWeight();
        if (tw != null || lw != null) {
            if (tw == null || lw == null) {
                return "测试权重和 LLM 权重必须同时设置";
            }
            if (tw < 0 || tw > 100 || lw < 0 || lw > 100) {
                return "权重必须在 0-100 之间";
            }
            if (tw + lw != 100) {
                return "测试权重 + LLM 权重必须等于 100, 当前为 " + (tw + lw);
            }
        }

        Integer a = hw.getGradeAThreshold();
        Integer b = hw.getGradeBThreshold();
        Integer c = hw.getGradeCThreshold();
        if (a != null || b != null || c != null) {
            if (a == null || b == null || c == null) {
                return "A/B/C 三个等级阈值必须同时设置";
            }
            if (a > 100 || c < 0) {
                return "等级阈值必须在 0-100 之间";
            }
            if (!(a > b && b > c)) {
                return "等级阈值必须满足 A > B > C, 当前 A=" + a + " B=" + b + " C=" + c;
            }
        }

        String dimsJson = hw.getLlmDimensions();
        if (dimsJson != null && !dimsJson.trim().isEmpty()) {
            List<LlmDimension> dims;
            try {
                dims = objectMapper.readValue(dimsJson, new TypeReference<List<LlmDimension>>() {});
            } catch (Exception e) {
                return "LLM 评分维度格式错误: " + e.getMessage();
            }
            if (dims == null || dims.isEmpty()) {
                return "LLM 评分维度不能为空数组";
            }
            if (dims.size() > 5) {
                return "LLM 评分维度最多 5 个";
            }
            int weightSum = 0;
            for (LlmDimension d : dims) {
                if (d.getName() == null || d.getName().trim().isEmpty()) {
                    return "LLM 评分维度名称不能为空";
                }
                if (d.getWeight() == null || d.getWeight() < 0 || d.getWeight() > 100) {
                    return "LLM 评分维度权重必须在 0-100 之间";
                }
                weightSum += d.getWeight();
            }
            if (weightSum != 100) {
                return "LLM 评分维度权重之和必须等于 100, 当前为 " + weightSum;
            }
        }

        return null;
    }

    /**
     * 删除作业（教师）
     */
    @DeleteMapping("/teacher/homework/{id}")
    public Result<Void> delete(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        Integer teacherId = currentUserId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        if (!homeworkOwnershipService.isHomeworkOwnedBy(id, teacherId)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        homeworkMapper.delete(id);
        return Result.success(null);
    }

    // ==================== 学生接口 ====================

    /**
     * 获取作业列表（学生，带提交状态）
     */
    @GetMapping("/student/homework/list")
    public Result<List<HomeworkWithStatus>> listForStudent(
            @RequestHeader("Authorization") String authHeader) {
        Integer studentId = currentUserId(authHeader);
        if (studentId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }

        List<Homework> homeworkList = homeworkMapper.findAll();
        List<HomeworkWithStatus> result = new ArrayList<>();

        for (Homework homework : homeworkList) {
            HomeworkWithStatus dto = new HomeworkWithStatus();
            dto.setHomework(homework);

            // 查询提交状态
            Submission submission = submissionMapper.findByHomeworkIdAndStudentId(homework.getId(), studentId);
            if (submission == null) {
                dto.setSubmitStatus(null); // 未提交
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
            @RequestHeader("Authorization") String authHeader) {
        Integer studentId = currentUserId(authHeader);
        if (studentId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Submission submission = submissionMapper.findByHomeworkIdAndStudentId(homeworkId, studentId);
        return Result.success(submission);
    }

    private Integer currentUserId(String authHeader) {
        return jwtUtils.getUserIdFromHeader(authHeader);
    }
}
