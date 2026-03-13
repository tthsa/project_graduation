package com.codereview.backend.controller;

import com.codereview.backend.common.ErrorCode;
import com.codereview.backend.common.Result;
import com.codereview.backend.dto.CodeSubmitRequest;
import com.codereview.backend.dto.ExecutionResult;
import com.codereview.backend.exception.BusinessException;
import com.codereview.backend.service.CodeSubmitService;
import com.codereview.backend.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码评审控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
public class CodeReviewController {

    private final CodeSubmitService codeSubmitService;
    private final ResultService resultService;

    /**
     * 提交代码评审任务
     *
     * POST /api/v1/code/submit
     *
     * @param request 提交请求
     * @return 任务 ID
     */
    @PostMapping("/submit")
    public Result<Map<String, String>> submitCode(@Valid @RequestBody CodeSubmitRequest request) {
        log.info("📨 收到代码提交请求: className={}", request.getClassName());

        // 验证代码不为空
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "代码不能为空");
        }

        // 提交任务
        String taskId = codeSubmitService.submitCode(request);

        // 返回任务 ID
        Map<String, String> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("status", "pending");

        log.info("✅ 任务提交成功: taskId={}", taskId);
        return Result.success("任务提交成功", data);
    }

    /**
     * 查询任务结果
     *
     * GET /api/v1/code/result/{taskId}
     *
     * @param taskId 任务 ID
     * @return 执行结果
     */
    @GetMapping("/result/{taskId}")
    public Result<ExecutionResult> getResult(@PathVariable String taskId) {
        log.info("🔍 查询任务结果: taskId={}", taskId);

        // 查询结果
        ExecutionResult result = resultService.getResult(taskId);

        if (result == null) {
            // 检查任务是否存在
            if (!resultService.exists(taskId)) {
                throw new BusinessException(ErrorCode.TASK_NOT_FOUND, "任务不存在或已过期");
            }

            // 任务还在处理中
            Map<String, String> pendingData = new HashMap<>();
            pendingData.put("taskId", taskId);
            pendingData.put("status", "processing");

            return Result.success("任务处理中，请稍后查询", null);
        }

        log.info("✅ 查询成功: taskId={}, success={}", taskId, result.getSuccess());
        return Result.success(result);
    }

    /**
     * 检查任务状态
     *
     * GET /api/v1/code/status/{taskId}
     *
     * @param taskId 任务 ID
     * @return 任务状态
     */
    @GetMapping("/status/{taskId}")
    public Result<Map<String, Object>> checkStatus(@PathVariable String taskId) {
        log.info("🔍 检查任务状态: taskId={}", taskId);

        Map<String, Object> statusData = new HashMap<>();
        statusData.put("taskId", taskId);

        // 检查结果是否存在
        ExecutionResult result = resultService.getResult(taskId);

        if (result != null) {
            // 任务已完成
            statusData.put("status", "completed");
            statusData.put("success", result.getSuccess());
            statusData.put("executionTime", result.getExecutionTime());
        } else if (resultService.exists(taskId)) {
            // 任务还在处理中
            statusData.put("status", "processing");
        } else {
            // 任务不存在
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND, "任务不存在或已过期");
        }

        return Result.success(statusData);
    }
}