package com.javaevaluation.service;

import com.javaevaluation.entity.EvaluationResult;  // ✅ 改为 entity 包
import com.javaevaluation.entity.Submission;
import com.javaevaluation.mapper.EvaluationResultMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResultService {

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    // 查询缓存
    private final Map<Integer, EvaluationResult> cache = new ConcurrentHashMap<>();

    /**
     * 获取评测结果
     */
    public EvaluationResult getResult(Integer submissionId) {
        // 1. 先查缓存
        EvaluationResult cached = cache.get(submissionId);
        if (cached != null) {
            return cached;
        }

        // 2. 查数据库
        EvaluationResult result = evaluationResultMapper.findBySubmissionId(submissionId);
        if (result != null) {
            cache.put(submissionId, result);
        }
        return result;
    }

    /**
     * 清除缓存
     */
    public void evictCache(Integer submissionId) {
        cache.remove(submissionId);
    }
}