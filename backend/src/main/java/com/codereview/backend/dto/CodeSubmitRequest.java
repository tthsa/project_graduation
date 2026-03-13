package com.codereview.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import lombok.Data;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CodeSubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Java 源代码
     */
    @NotBlank(message = "代码不能为空")
    @Size(max = 10000, message = "代码长度不能超过 10000 字符")
    private String code;

    /**
     * 类名（默认 Main）
     */
    private String className;

    /**
     * 时间限制（秒），默认 10 秒
     */
    @Min(value = 1, message = "时间限制最小为 1 秒")
    @Max(value = 60, message = "时间限制最大为 60 秒")
    private Integer timeLimit;

    /**
     * 内存限制（MB），默认 256 MB
     */
    @Min(value = 64, message = "内存限制最小为 64 MB")
    @Max(value = 1024, message = "内存限制最大为 1024 MB")
    private Integer memoryLimit;
}
