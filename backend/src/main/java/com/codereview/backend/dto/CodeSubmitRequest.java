package com.codereview.backend.dto;

import lombok.Data;
@Data

public class CodeSubmitRequest {
    /**Java 源代码*/
    private String code;

    /**类名（默认 Main）*/
    private String className;

    /** 时间限制（秒，默认 10）*/
    private Integer timeLimit;

    /**内存限制（MB，默认 256）*/
    private Integer memoryLimit;

}
