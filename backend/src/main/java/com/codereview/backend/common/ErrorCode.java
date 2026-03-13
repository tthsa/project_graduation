package com.codereview.backend.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    NOT_FOUND(404, "资源不存在"),
    TASK_NOT_FOUND(4041, "任务不存在"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SUBMIT_FAILED(5001, "任务提交失败"),
    DOCKER_ERROR(5002, "Docker 执行失败"),
    LLM_ERROR(5003, "LLM 评审失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}