package com.codereview.backend.dto;

import lombok.Builder;
import lombok.Data;
@Data
@Builder

public class CodeTask {
    /**任务 ID*/
    private String taskId;

    /**源代码*/
    private String code;

    /**类名*/
    private String className;

    /**时间限制（秒）*/
    private Integer timeLimit;

    /**内存限制（MB）*/
    private Integer memoryLimit;

    /**创建时间戳*/
    private Long timestamp;

}
