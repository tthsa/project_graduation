package com.javaevaluation.service;

import com.javaevaluation.dto.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 结果存储服务
 * 用于存储和查询评测结果
 */
@Slf4j
@Service
public class ResultService {

    private final Map<String, ExecutionResult> resultCache = new ConcurrentHashMap<>();

    /**
     * 保存结果
     */
    public void saveResult(String taskId, ExecutionResult result) {
        resultCache.put(taskId, result);
        log.info("保存评测结果: taskId={}", taskId);
    }

    /**
     * 获取结果
     */
    public ExecutionResult getResult(String taskId) {
        return resultCache.get(taskId);
    }

    /**
     * 删除结果
     */
    public void deleteResult(String taskId) {
        resultCache.remove(taskId);
        log.info("删除评测结果: taskId={}", taskId);
    }

    /**
     * 检查结果是否存在
     */
    public boolean hasResult(String taskId) {
        return resultCache.containsKey(taskId);
    }
}