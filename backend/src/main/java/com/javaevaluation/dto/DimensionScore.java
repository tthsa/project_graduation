package com.javaevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个 LLM 维度的得分。例如 {name:"代码质量", score:8}
 * score 为 0-10，null 表示该维度未提取到分数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionScore {
    private String name;
    private Integer score;
}
