package com.javaevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 评分维度配置。例如 {name:"代码质量", weight:60}
 * 一个作业的多个维度 weight 之和应为 100。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmDimension {
    private String name;
    private Integer weight;
}
