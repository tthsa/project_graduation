package com.javaevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 执行结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult implements Serializable {

    /** 任务ID */
    private String taskId;

    /** 编译状态: SUCCESS, COMPILE_ERROR, ERROR */
    private String compileStatus;

    /** 通过的测试用例数 */
    private Integer testPassed;

    /** 总测试用例数 */
    private Integer testTotal;

    /** 测试得分 */
    private Integer testScore;

    /** 输出内容 */
    private String output;

    /** 错误信息 */
    private String errorMessage;
}