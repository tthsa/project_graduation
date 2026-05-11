package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.TestCaseMapper;
import com.javaevaluation.security.JwtUtils;
import com.javaevaluation.service.HomeworkOwnershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用例控制器
 */
@RestController
@RequestMapping("/api/teacher/testcase")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseMapper testCaseMapper;
    private final JwtUtils jwtUtils;
    private final HomeworkOwnershipService homeworkOwnershipService;

    /**
     * 根据作业ID获取测试用例列表
     */
    @GetMapping("/list")
    public Result<List<TestCase>> list(@RequestParam Integer homeworkId) {
        List<TestCase> testCases = testCaseMapper.findByHomeworkId(homeworkId);
        return Result.success(testCases);
    }

    /**
     * 获取测试用例详情
     */
    @GetMapping("/{id}")
    public Result<TestCase> getById(@PathVariable Integer id) {
        TestCase testCase = testCaseMapper.findById(id);
        if (testCase == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(testCase);
    }

    /**
     * 创建测试用例（教师）
     */
    @PostMapping("/create")
    public Result<TestCase> create(@Valid @RequestBody TestCase testCase) {
        testCaseMapper.insert(testCase);
        return Result.success(testCase);
    }

    /**
     * 更新测试用例（教师）
     */
    @PutMapping("/{id}")
    public Result<TestCase> update(
            @PathVariable Integer id,
            @Valid @RequestBody TestCase testCase,
            @RequestHeader("Authorization") String authHeader) {
        TestCase existingTestCase = testCaseMapper.findById(id);
        if (existingTestCase == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        if (!homeworkOwnershipService.isTestCaseOwnedBy(id, teacherId)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        testCase.setId(id);
        testCaseMapper.update(testCase);
        return Result.success(testCase);
    }

    /**
     * 删除测试用例（教师）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        TestCase testCase = testCaseMapper.findById(id);
        if (testCase == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        if (!homeworkOwnershipService.isTestCaseOwnedBy(id, teacherId)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        testCaseMapper.delete(id);
        return Result.success(null);
    }

    private Integer currentTeacherId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return null;
        }
        return jwtUtils.getUserIdFromToken(token);
    }
}
