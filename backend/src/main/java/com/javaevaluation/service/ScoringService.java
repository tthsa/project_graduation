package com.javaevaluation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaevaluation.dto.DimensionScore;
import com.javaevaluation.dto.LlmDimension;
import com.javaevaluation.entity.Homework;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评分服务: 将 (testScore, llmScore, Homework 配置) 汇总为 final_score 和 grade。
 *
 * 算法:
 *   - llm_score 为 null && test_score 非 null → final = test_score (LLM 缺失只算测试)
 *   - test_score 为 null && llm_score 非 null → final = llm_score * 10 (无测试用例只算 LLM)
 *   - 两者均 null → final = null
 *   - 否则 → final = round(test_score * test_weight/100 + llm_score * 10 * llm_weight/100)
 *
 * 等级:
 *   final >= grade_a_threshold → A
 *   final >= grade_b_threshold → B
 *   final >= grade_c_threshold → C
 *   final >= 0                 → D
 *   final == null              → null
 */
@Slf4j
@Service
public class ScoringService {

    private static final int DEFAULT_TEST_WEIGHT = 70;
    private static final int DEFAULT_LLM_WEIGHT = 30;
    private static final int DEFAULT_GRADE_A = 90;
    private static final int DEFAULT_GRADE_B = 75;
    private static final int DEFAULT_GRADE_C = 60;

    public Integer computeFinalScore(Integer testScore, Integer llmScore, Homework homework) {
        if (testScore == null && llmScore == null) return null;
        if (llmScore == null) return clamp(testScore, 0, 100);
        if (testScore == null) return clamp(llmScore * 10, 0, 100);

        int testWeight = orDefault(homework == null ? null : homework.getTestWeight(), DEFAULT_TEST_WEIGHT);
        int llmWeight = orDefault(homework == null ? null : homework.getLlmWeight(), DEFAULT_LLM_WEIGHT);
        double weighted = testScore * (testWeight / 100.0) + (llmScore * 10) * (llmWeight / 100.0);
        return clamp((int) Math.round(weighted), 0, 100);
    }

    public String computeGrade(Integer finalScore, Homework homework) {
        if (finalScore == null) return null;
        int a = orDefault(homework == null ? null : homework.getGradeAThreshold(), DEFAULT_GRADE_A);
        int b = orDefault(homework == null ? null : homework.getGradeBThreshold(), DEFAULT_GRADE_B);
        int c = orDefault(homework == null ? null : homework.getGradeCThreshold(), DEFAULT_GRADE_C);
        if (finalScore >= a) return "A";
        if (finalScore >= b) return "B";
        if (finalScore >= c) return "C";
        return "D";
    }

    /**
     * 将各维度得分按权重加权平均, 输出 0-10 的整数 llm_score。
     * - 维度全部 null → 返回 null
     * - 部分 null → 该维度被忽略且其权重从有效权重中扣除（按已有得分归一化）
     * - dimensions 为空 / 配置缺失 → 返回 null
     */
    public Integer aggregateLlmScore(List<DimensionScore> scores, List<LlmDimension> dimensions) {
        if (scores == null || scores.isEmpty() || dimensions == null || dimensions.isEmpty()) {
            return null;
        }

        Map<String, Integer> weightByName = new HashMap<>();
        for (LlmDimension d : dimensions) {
            if (d != null && d.getName() != null && d.getWeight() != null) {
                weightByName.put(d.getName(), d.getWeight());
            }
        }

        double weightedSum = 0.0;
        int weightTotal = 0;
        for (DimensionScore s : scores) {
            if (s == null || s.getScore() == null) continue;
            Integer w = weightByName.get(s.getName());
            if (w == null || w <= 0) continue;
            weightedSum += s.getScore() * w;
            weightTotal += w;
        }
        if (weightTotal == 0) return null;
        double avg = weightedSum / weightTotal;
        long rounded = Math.round(avg);
        return clamp((int) rounded, 0, 10);
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private int orDefault(Integer v, int def) {
        return v == null ? def : v;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析 homework.llmDimensions JSON 为维度列表。
     * - null / 空 / 解析失败 → LlmReviewService.DEFAULT_DIMENSIONS（单一维度）
     */
    public List<LlmDimension> parseDimensions(String json) {
        if (json == null || json.trim().isEmpty()) {
            return LlmReviewService.DEFAULT_DIMENSIONS;
        }
        try {
            List<LlmDimension> parsed = objectMapper.readValue(json, new TypeReference<List<LlmDimension>>() {});
            if (parsed == null || parsed.isEmpty()) {
                return LlmReviewService.DEFAULT_DIMENSIONS;
            }
            return parsed;
        } catch (Exception e) {
            log.warn("解析 llmDimensions 失败, 使用默认单维度: {}", e.getMessage());
            return LlmReviewService.DEFAULT_DIMENSIONS;
        }
    }

    /**
     * 把 DimensionScore 列表序列化成 JSON 字符串，用于存 evaluation_result.llm_dimension_scores。
     * 失败返回 null。
     */
    public String serializeDimensionScores(List<DimensionScore> scores) {
        if (scores == null || scores.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(scores);
        } catch (Exception e) {
            log.warn("序列化 dimensionScores 失败: {}", e.getMessage());
            return null;
        }
    }
}
