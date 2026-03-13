package com.codereview.backend.service.consumer;

import com.codereview.backend.dto.CodeTask;
import com.codereview.backend.dto.ExecutionResult;
import com.codereview.backend.service.DockerSandboxService;
import com.codereview.backend.service.LlmReviewService;
import com.codereview.backend.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 任务处理器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProcessor {

    private final DockerSandboxService dockerSandboxService;
    private final LlmReviewService llmReviewService;
    private final ResultService resultService;

    /**
     * 处理代码任务
     *
     * @param task 代码任务
     */
    public void processTask(CodeTask task) {
        log.info("🔄 开始处理任务: taskId={}", task.getTaskId());

        try {
            // 1. 在 Docker 沙箱中执行代码
            ExecutionResult result = dockerSandboxService.executeInSandbox(task);
            log.info("✅ 代码执行完成: taskId={}, success={}", task.getTaskId(), result.getSuccess());

            // 2. 调用 LLM 进行代码评审
            String llmReview = llmReviewService.reviewCode(task.getCode(), result);
            result.setLlmReview(llmReview);
            log.info("✅ LLM 评审完成: taskId={}", task.getTaskId());

            // 3. 保存结果
            resultService.saveResult(result);
            log.info("💾 结果已保存: taskId={}", task.getTaskId());

        } catch (Exception e) {
            log.error("❌ 任务处理失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);

            // 保存失败结果
            ExecutionResult errorResult = ExecutionResult.builder()
                    .taskId(task.getTaskId())
                    .success(false)
                    .error("任务处理失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            resultService.saveResult(errorResult);
        }
    }
}