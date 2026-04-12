package com.javaevaluation.common;

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
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    TASK_NOT_FOUND(4041, "任务不存在"),

    // 认证相关错误 4xx
    LOGIN_FAILED(4001, "用户名或密码错误"),
    TOKEN_INVALID(4002, "Token无效"),
    TOKEN_EXPIRED(4003, "Token已过期"),
    USER_NOT_FOUND(4004, "用户不存在"),
    USER_TYPE_ERROR(4005, "用户类型错误"),

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