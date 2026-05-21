package com.javaevaluation.service;

import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.mapper.EvaluationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final EvaluationResultMapper evaluationResultMapper;

    /**
     * 获取评测结果（带缓存）
     */
    @Cacheable(value = "evaluationResult", key = "#submissionId")
    public EvaluationResult getResult(Integer submissionId) {
        return evaluationResultMapper.findBySubmissionId(submissionId);
    }

    /**
     * 清除缓存
     */
    @CacheEvict(value = "evaluationResult", key = "#submissionId")
    public void evictCache(Integer submissionId) {
        // Spring Cache 自动处理清除
    }
}
