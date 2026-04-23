package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.TestCaseMapper;
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
    public Result<TestCase> update(@PathVariable Integer id, @Valid @RequestBody TestCase testCase) {
        TestCase existingTestCase = testCaseMapper.findById(id);
        if (existingTestCase == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        testCase.setId(id);
        testCaseMapper.update(testCase);
        return Result.success(testCase);
    }

    /**
     * 删除测试用例（教师）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        TestCase testCase = testCaseMapper.findById(id);
        if (testCase == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        testCaseMapper.delete(id);
        return Result.success(null);
    }
}